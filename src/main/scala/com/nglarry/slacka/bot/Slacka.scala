package com.nglarry.slacka.bot

import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.actor._
import akka.routing.RoundRobinPool
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.ws.{WebSocket, WebSocketTextListener}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import com.nglarry.slacka.api.{SlackError, SlackResponseHandler, SlackWebApi, SlackWebSocketApi}
import com.nglarry.slacka.codecs.responses.RtmStart
import com.nglarry.slacka.codecs.{isHello, isReply}
import com.nglarry.slacka.util.randomReplyId

object Slacka {
  val DefaultNrWorkers = 4
  val DefaultPingInterval = 5.seconds
  val MaxConnectAttempts = 3

  def apply(token: String) = PropsBuilder(token)

  case class PropsBuilder(
      token: String,
      webConfig: AsyncHttpClientConfig = SlackWebApi.defaultHttpClientConfig,
      websocketConfig: AsyncHttpClientConfig = SlackWebSocketApi.defaultHttpClientConfig,
      listeners: List[RealTimeMessagingListener] = List.empty,
      workerCount: Int = DefaultNrWorkers,
      pingInterval: FiniteDuration = DefaultPingInterval) {
    def withWebConfig(config: AsyncHttpClientConfig) = copy(webConfig = config)
    def withWebSocketConfig(config: AsyncHttpClientConfig) = copy(websocketConfig = config)
    def withListeners(listeners: List[RealTimeMessagingListener]) = copy(listeners = listeners)
    def withWorkerCount(count: Int) = copy(workerCount = count)
    def withPingInterval(duration: FiniteDuration) = copy(pingInterval = duration)
    def addListener(listener: RealTimeMessagingListener) = copy(listeners = listeners :+ listener)

    def build: Props = {
      Props(classOf[Slacka], token, webConfig, websocketConfig, listeners, workerCount, pingInterval)
    }
  }
}

class Slacka(
    token: String,
    webConfig: AsyncHttpClientConfig,
    websocketConfig: AsyncHttpClientConfig,
    listeners: List[RealTimeMessagingListener],
    workerCount: Int,
    pingInterval: FiniteDuration) extends SlackaActor {
  import com.nglarry.slacka.bot.BotMessages._
  import com.nglarry.slacka.bot.Slacka._

  val system = context.system
  import system.dispatcher

  val webApi: SlackWebApi = SlackWebApi(token, webConfig)

  val wsApi: SlackWebSocketApi = SlackWebSocketApi(websocketConfig)

  val websocketListener = new WebSocketTextListener {
    override def onMessage(message: String): Unit = {
      log.debug(s"Received: $message")
      self ! WebSocketMessage(message)
    }
    override def onOpen(websocket: WebSocket): Unit = {}
    override def onClose(websocket: WebSocket): Unit = { self ! WebSocketClose }
    override def onError(t: Throwable): Unit = { self ! WebSocketThrowable(t) }
  }

  val workers: ActorRef =
    context.actorOf(RoundRobinPool(workerCount).props(Props(classOf[Worker], listeners)), "workers")

  var state: Option[SlackState] = None

  var pinger: Option[Cancellable] = None

  var connecter: Option[Cancellable] = None

  override def receive: Receive = disconnected

  override def preStart(): Unit = {
    connect(0)
  }

  override def postStop(): Unit = {
    connecter.foreach { _.cancel() }
    pinger.foreach { _.cancel() }
    webApi.close()
    wsApi.close()
  }

  private def connect(attempt: Int): Unit = attempt match {
    case 0 =>
      self ! Connect(attempt)
    case a if a < MaxConnectAttempts =>
      val delay = (attempt * attempt).seconds
      log.info(s"Connecting in $delay")
      connecter = Some(system.scheduler.scheduleOnce(delay, self, Connect(attempt)))
    case _ =>
      log.error("Too many failed attempts. Stopping...")
      context.stop(self)
  }

  private def disconnected: Receive = {
    case Connect(attempt) =>
      context.become(connecting)
      log.info("Fetching WebSocket URL and start state (attempt #{})", attempt)
      val startHandler = new SlackResponseHandler[RtmStart] {
        override def onSuccess(start: RtmStart): Unit = {
          self ! ConnectWebSocket(start, attempt)
        }
        override def onSlackError(error: SlackError): Unit = {
          self ! StartError(error, attempt)
        }
        override def onThrowable(t: Throwable): Unit = {
          self ! StartThrowable(t, attempt)
        }
      }
      webApi.rtm.start(handler = startHandler)
  }

  private def connecting: Receive = {
    case ConnectWebSocket(start, attempt) =>
      log.info("Connecting via WebSockets")
      wsApi.connect(start.url, websocketListener) match {
        case Success(true) =>
          log.info("WebSockets connection established")
          state = Some(SlackState(start))
          context.become(connected)
          pinger = startPing()
        case Success(false) =>
          context.become(disconnected)
          sender() ! StartThrowable(new RuntimeException("Connected but lost WebSockets connection"), attempt)
        case Failure(t) =>
          context.become(disconnected)
          sender() ! StartThrowable(t, attempt)
      }
    case WebSocketMessage(message) =>
      if (isHello(parse(message).asInstanceOf[JObject])) {
        state.foreach { self ! Connected(_) }
      }
    case StartError(error, attempt) =>
      log.error("Slack error during initialization (attempt #{}): {}", attempt, error)
      context.become(disconnected)
      connect(attempt + 1)
    case StartThrowable(t, attempt) =>
      log.error(t, "Exception during initialization (attempt #{})", attempt)
      context.become(disconnected)
      connect(attempt + 1)
  }

  private def connected: Receive = {
    case WebSocketClose =>
      log.error("WebSockets disconnected. Reconnecting")
      context.become(disconnected)
      pinger.foreach { _.cancel() }
      wsApi.disconnect()
      state.foreach { workers ! Disconnected(_) }
      connect(0)
    case WebSocketThrowable(t) =>
      log.error(t, "WebSockets error")
    case WebSocketMessage(message) =>
      val json = parse(message).asInstanceOf[JObject]
      if (!isReply(json)) {
        state = state.map(_.update(json))
      }
      state.foreach { s =>
        workers ! ReceiveMessage(s, json)
      }
    case SendMessage(id, channel, text) =>
      wsApi.sendMessage(id, channel, text)
    case SendPing(id) =>
      wsApi.sendPing(id)
  }

  private def startPing() =
    Some(system.scheduler.schedule(pingInterval, pingInterval, self, SendPing(randomReplyId)))
}



