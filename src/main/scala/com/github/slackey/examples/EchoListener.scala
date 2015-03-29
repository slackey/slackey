package com.github.slackey.examples

import org.json4s._

import com.github.slackey.bot.{BotMessages, RealTimeMessagingListener, SlackState}
import com.github.slackey.codecs.types.SimpleMessage
import com.github.slackey.util.randomReplyId

object EchoListener extends RealTimeMessagingListener {
  import BotMessages._

  override def onSimpleMessage(state: SlackState, message: SimpleMessage, json: JObject): Seq[SendMessage] = {
    val user = state.userById.get(message.user)
    val name = user.fold("<unknown>")(_.name)
    val replyId = randomReplyId
    val text = s"$name said: ${message.text}"
    Seq(SendMessage(replyId, message.channel, text))
  }

}
