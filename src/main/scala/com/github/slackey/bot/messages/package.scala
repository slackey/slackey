package com.github.slackey.bot

import org.json4s.JObject

import com.github.slackey.codecs.responses.RtmStart

package object messages {
  case class FetchStart(attempt: Int)
  case class ConnectWebSocket(start: RtmStart, attempt: Int)
  case class StartError(msg: String, attempt: Int)
  case class StartThrowable(t: Throwable, attempt: Int)

  case class WebSocketMessage(message: String)
  case object WebSocketClose
  case class WebSocketThrowable(t: Throwable)

  case class Connected(state: SlackState)
  case class Disconnected(state: SlackState)
  case class ReceiveMessage(state: SlackState, json: JObject)
  case class SendMessage(id: Long, channel: String, text: String)
  case class SendPing(id: Long)
}