package com.nglarry.slacka.api

import scala.util.Try

import com.ning.http.client.ws.{WebSocket, WebSocketTextListener, WebSocketUpgradeHandler}
import com.ning.http.client.{AsyncHttpClient, AsyncHttpClientConfig}
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._

object SlackWebSocketApi {
  def defaultHttpClientConfig =
    new AsyncHttpClientConfig.Builder().build()

  def apply(clientConfig: AsyncHttpClientConfig) =
    new SlackWebSocketApi(clientConfig)
}

class SlackWebSocketApi(clientConfig: AsyncHttpClientConfig) {

  private val client = new AsyncHttpClient(clientConfig)
  private var websocket: Option[WebSocket] = None

  def connect(url: String, listener: WebSocketTextListener): Try[Boolean] = {
    val upgradeHandler = new WebSocketUpgradeHandler.Builder().addWebSocketListener(listener).build()
    Try {
      val ws = client.prepareGet(url).execute(upgradeHandler).get()
      websocket = Some(ws)
      ws.isOpen
    }
  }

  def isConnected: Boolean = websocket.filter(_.isOpen).isDefined

  def disconnect(): Unit = { websocket.foreach(_.close()) }

  def close(): Unit = {
    websocket.foreach { _.close() }
    client.close()
  }

  def sendMessage(id: Long, channel: String, text: String): Unit = send {
    ("id" -> id) ~
    ("type" -> "message") ~
    ("channel" -> channel) ~
    ("text" -> text)
  }

  def sendTyping(id: Long, channel: String): Unit = send {
    ("id" -> id) ~
    ("type" -> "typing") ~
    ("channel" -> channel)
  }

  def sendPing(id: Long): Unit = send {
    ("id" -> id) ~
    ("type" -> "ping")
  }

  def send(json: JObject): Unit = {
    websocket.foreach { _.sendMessage(compact(render(json))) }
  }

}
