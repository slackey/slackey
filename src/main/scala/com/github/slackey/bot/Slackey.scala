package com.github.slackey.bot

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.duration._
import scala.util.{Random, Failure, Success}

import akka.actor._
import akka.routing.RoundRobinPool
import com.ning.http.client.AsyncHttpClientConfig
import com.ning.http.client.ws.{WebSocket, WebSocketTextListener}
import org.json4s._
import org.json4s.jackson.JsonMethods._

import com.github.slackey.api.{SlackError, SlackResponseHandler, SlackWebApi, SlackWebSocketApi}
import com.github.slackey.bot.messages._
import com.github.slackey.codecs.responses.RtmStart
import com.github.slackey.codecs.{isHello, isReply}

object Slackey {
  val DefaultNrWorkers = 4
  val DefaultPingInterval = 5.seconds
  val DefaultWebExecutorServiceFactory = () => Executors.newCachedThreadPool()
  val DefaultWebSocketExecutorServiceFactory = () => Executors.newCachedThreadPool()
  val MaxConnectAttempts = 3

  def apply(token: String) = PropsBuilder(token)

  case class PropsBuilder(
      token: String,
      webConfig: AsyncHttpClientConfig = SlackWebApi.defaultHttpClientConfig,
      websocketConfig: AsyncHttpClientConfig = SlackWebSocketApi.defaultHttpClientConfig,
      webExecutorServiceFactory: () => ExecutorService = DefaultWebExecutorServiceFactory,
      webSocketExecutorServiceFactory: () => ExecutorService = DefaultWebSocketExecutorServiceFactory,
      listeners: List[RealTimeMessagingListener] = List.empty,
      workerCount: Int = DefaultNrWorkers,
      pingInterval: FiniteDuration = DefaultPingInterval) {
    def withWebConfig(config: AsyncHttpClientConfig) = copy(webConfig = config)
    def withWebSocketConfig(config: AsyncHttpClientConfig) = copy(websocketConfig = config)
    def withWebExecutorServiceFactory(factory: () => ExecutorService) = copy(webExecutorServiceFactory = factory)
    def withWebSocketExecutorServiceFactory(factory: () => ExecutorService) = copy(webSocketExecutorServiceFactory = factory)
    def withListeners(listeners: List[RealTimeMessagingListener]) = copy(listeners = listeners)
    def withWorkerCount(count: Int) = copy(workerCount = count)
    def withPingInterval(duration: FiniteDuration) = copy(pingInterval = duration)
    def addListener(listener: RealTimeMessagingListener) = copy(listeners = listeners :+ listener)

    def build: Props = {
      Props(classOf[Slackey],
        token,
        webConfig,
        websocketConfig,
        webExecutorServiceFactory,
        webSocketExecutorServiceFactory,
        listeners,
        workerCount,
        pingInterval)
    }
  }
}

class Slackey(
    token: String,
    webConfig: AsyncHttpClientConfig,
    websocketConfig: AsyncHttpClientConfig,
    webExecutorServiceFactory: () => ExecutorService,
    webSocketExecutorServiceFactory: () => ExecutorService,
    listeners: List[RealTimeMessagingListener],
    workerCount: Int,
    pingInterval: FiniteDuration) extends SlackeyActor {
  import Slackey._

  val system = context.system
  import system.dispatcher

  val webApi: SlackWebApi = {
    val config = new AsyncHttpClientConfig.Builder(webConfig)
      .setExecutorService(webExecutorServiceFactory())
      .build()
    SlackWebApi(token, config)
  }

  val wsApi: SlackWebSocketApi = {
    val config = new AsyncHttpClientConfig.Builder(websocketConfig)
      .setExecutorService(webSocketExecutorServiceFactory())
      .build()
    SlackWebSocketApi(config)
  }

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
    wsApi.close()
  }

  private def connect(attempt: Int): Unit = attempt match {
    case 0 =>
      self ! FetchStart(attempt)
    case a if a < MaxConnectAttempts =>
      val delay = (attempt * attempt).seconds
      log.info(s"Connecting in $delay")
      connecter = Some(system.scheduler.scheduleOnce(delay, self, FetchStart(attempt)))
    case _ =>
      log.error("Too many failed attempts. Stopping...")
      context.stop(self)
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
      wsApi.connect(start.url, websocketListener) match {
        case Success(true) =>
          log.info("WebSockets connection established")
        case Success(false) =>
          context.become(disconnected)
          self ! StartThrowable(new RuntimeException("Connected but lost WebSockets connection"), attempt)
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
      wsApi.disconnect()
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
      wsApi.sendMessage(id, channel, text)
    case SendPing(id) =>
      wsApi.sendPing(id)
  }

  private def startPing() =
    Some(system.scheduler.schedule(pingInterval, pingInterval, self, SendPing(Random.nextLong())))
}



