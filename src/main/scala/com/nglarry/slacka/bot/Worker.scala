package com.nglarry.slacka.bot

import com.nglarry.slacka.codecs.types.Message
import com.nglarry.slacka.codecs.{extract, isReply}
import org.json4s._

class Worker(listeners: List[RealTimeMessagingListener]) extends SlackaActor {
  import BotMessages._

  override def receive: Receive = {
    case Connected(state) =>
      dispatchAndReply(_.onConnected(state))
    case Disconnected(state) =>
      dispatchAndReply(_.onDisconnected(state))
    case ReceiveMessage(state, json) =>
      if (isReply(json)) handleReply(state, json) else handle(state, json)
  }

  private def handle(state: SlackState, json: JObject) = {
    json \ "type" match {
      case JString("message") =>
        val message = extract[Message](json)
        message.subtype match {
          case None =>
            dispatchAndReply(_.onSimpleMessage(state, message, json))
          case Some("bot_message") =>
            dispatchAndReply(_.onBotMessage(state, message, json))
          case Some("me_message") =>
            dispatchAndReply(_.onMeMessage(state, message, json))
          case _ => // noop
        }
      case _ =>  // noop
    }
    for (listener <- listeners) {
      listener.onAnyMessage(state, json)
    }
  }

  private def handleReply(state: SlackState, json: JObject) = {}

  private def dispatchAndReply(f: RealTimeMessagingListener => Seq[SendMessage]) = {
    for {
      listener <- listeners
      msg <- f(listener)
    } sender() ! msg
  }
}
