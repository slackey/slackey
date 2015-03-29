package com.github.slackey.bot

import org.json4s._

import com.github.slackey.codecs.types.{BotMessage, MeMessage, Message, SimpleMessage}
import com.github.slackey.codecs.{extract, isReply}

class Worker(listeners: List[RealTimeMessagingListener]) extends SlackeyActor {
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
            val specific = SimpleMessage(message)
            dispatchAndReply(_.onSimpleMessage(state, specific, json))
          case Some("bot_message") =>
            val specific = BotMessage(message)
            dispatchAndReply(_.onBotMessage(state, specific, json))
          case Some("me_message") =>
            val specific = MeMessage(message)
            dispatchAndReply(_.onMeMessage(state, specific, json))
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
