package com.github.slackey.api

import com.ning.http.client.ws.WebSocket
import org.json4s.JObject
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods.{compact, render}

/**
 * Slack API wrapper around a WebSocket connection.
 */
object SlackWebSocketConnection {
  def apply(websocket: WebSocket): SlackWebSocketConnection =
    new SlackWebSocketConnection(websocket)
}

class SlackWebSocketConnection(websocket: WebSocket) {
  def isOpen: Boolean = websocket.isOpen

  def isClosed: Boolean = !isOpen

  def close(): Unit = websocket.close()

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
    websocket.sendMessage(compact(render(json)))
  }
}
