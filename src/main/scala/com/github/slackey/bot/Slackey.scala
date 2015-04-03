package com.github.slackey.bot

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.duration._
import scala.util.{Failure, Random, Success}

import akka.actor._
import akka.routing.RoundRobinPool
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.ws.{WebSocket, WebSocketTextListener}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import com.github.slackey.api.{SlackApi, SlackError, SlackResponseHandler, SlackWebSocketConnection}
import com.github.slackey.bot.messages._
import com.github.slackey.codecs.responses.RtmStart
import com.github.slackey.codecs.{isHello, isReply}

object Slackey {
  val DefaultNrWorkers = 4
  val DefaultPingInterval = 5.seconds
  val DefaultHttpExecutorServiceFactory = () => Executors.newCachedThreadPool()

  def apply(token: String) = PropsBuilder(token)

  case class PropsBuilder(
      token: String,
      httpConfig: AsyncHttpClientConfig = SlackApi.defaultHttpClientConfig,
      httpExecutorServiceFactory: () => ExecutorService = DefaultHttpExecutorServiceFactory,
      listeners: List[RealTimeMessagingListener] = List.empty,
      workerCount: Int = DefaultNrWorkers,
      pingInterval: FiniteDuration = DefaultPingInterval) {
    def withHttpConfig(config: AsyncHttpClientConfig) = copy(httpConfig = config)
    def withHttpExecutorServiceFactory(factory: () => ExecutorService) = copy(httpExecutorServiceFactory = factory)
    def withListeners(listeners: List[RealTimeMessagingListener]) = copy(listeners = listeners)
    def withWorkerCount(count: Int) = copy(workerCount = count)
    def withPingInterval(duration: FiniteDuration) = copy(pingInterval = duration)
    def addListener(listener: RealTimeMessagingListener) = copy(listeners = listeners :+ listener)

    def build: Props = {
      Props(classOf[Slackey],
        token,
        httpConfig,
        httpExecutorServiceFactory,
        listeners,
        workerCount,
        pingInterval)
    }
  }
}

class Slackey(
    token: String,
    httpConfig: AsyncHttpClientConfig,
    httpExecutorServiceFactory: () => ExecutorService,
    listeners: List[RealTimeMessagingListener],
    workerCount: Int,
    pingInterval: FiniteDuration) extends SlackeyActor {
  val system = context.system
  import system.dispatcher

  val webApi: SlackApi = {
    val config = new AsyncHttpClientConfig.Builder(httpConfig)
      .setExecutorService(httpExecutorServiceFactory())
      .build()
    SlackApi(token, config)
  }

  var wsConn: Option[SlackWebSocketConnection] = None

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
    wsConn.foreach { _.close() }
  }

  private def connect(attempt: Int): Unit = attempt match {
    case 0 =>
      self ! FetchStart(attempt)
    case _ =>
      val delay = Math.min(attempt * attempt, 60).seconds
      log.info(s"Connecting in $delay")
      connecter = Some(system.scheduler.scheduleOnce(delay, self, FetchStart(attempt)))
  }

  private def disconnected: Receive = {
    case FetchStart(attempt) =>
      log.info("Fetching WebSocket URL and start state (attempt #{})", attempt)
      val startHandler = new SlackResponseHandler[RtmStart] {
        override def onSuccess(start: RtmStart): Unit = {
          self ! ReceivedStart(start, attempt)
        }
        override def onSlackError(error: SlackError): Unit = {
          self ! StartError(error, attempt)
        }
        override def onThrowable(t: Throwable): Unit = {
          self ! StartThrowable(t, attempt)
        }
      }
      webApi.rtm.start(handler = startHandler)
    case ReceivedStart(start, attempt) =>
      log.info("Connecting via WebSockets")
      context.become(connecting(SlackState(start)))
      self ! ConnectWebSocket(start, attempt)
    case StartError(error, attempt) =>
      log.error("Slack error during initialization (attempt #{}): {}", attempt, error)
      connect(attempt + 1)
    case StartThrowable(t, attempt) =>
      log.error(t, "Exception during initialization (attempt #{})", attempt)
      connect(attempt + 1)
  }

  private def connecting(state: SlackState): Receive = {
    case ConnectWebSocket(start, attempt) =>
      context.become(awaitingHello(state))
      webApi.connect(start.url, websocketListener) match {
        case Success(conn) =>
          if (conn.isOpen) {
            log.info("WebSockets connection established")
            wsConn = Some(conn)
          } else {
            conn.close()
            context.become(disconnected)
            val ex = new RuntimeException("Connected but lost WebSockets connection")
            self ! StartThrowable(ex, attempt)
          }
        case Failure(t) =>
          context.become(disconnected)
          self ! StartThrowable(t, attempt)
      }
  }

  private def awaitingHello(state: SlackState): Receive = {
    case WebSocketMessage(message) =>
      if (isHello(parse(message).asInstanceOf[JObject])) {
        log.info("Received 'hello'")
        context.become(connected(state))
        pinger = startPing()
        workers ! Connected(state)
      }
  }

  private def connected(state: SlackState): Receive = {
    case WebSocketClose =>
      log.error("WebSockets disconnected. Reconnecting")
      context.become(disconnected)
      pinger.foreach { _.cancel() }
      wsConn.foreach { _.close() }
      workers ! Disconnected(state)
      connect(0)
    case WebSocketThrowable(t) =>
      log.error(t, "WebSockets error")
    case WebSocketMessage(message) =>
      val json = parse(message).asInstanceOf[JObject]
      if (!isReply(json)) {
        context.become(connected(state.update(json)))
      }
      workers ! ReceiveMessage(state, json)
    case SendMessage(id, channel, text) =>
      wsConn.foreach { _.sendMessage(id, channel, text) }
    case SendPing(id) =>
      wsConn.foreach { _.sendPing(id) }
  }

  private def startPing() =
    Some(system.scheduler.schedule(pingInterval, pingInterval, self, SendPing(Random.nextLong())))
}



