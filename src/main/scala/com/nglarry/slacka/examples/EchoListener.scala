package com.nglarry.slacka.examples

import org.json4s._

import com.nglarry.slacka.bot.{RealTimeMessagingListener, SlackState}
import com.nglarry.slacka.codecs.types.SimpleMessage
import com.nglarry.slacka.util.randomReplyId

object EchoListener extends RealTimeMessagingListener {
  import com.nglarry.slacka.bot.BotMessages._

  override def onSimpleMessage(state: SlackState, message: SimpleMessage, json: JObject): Seq[SendMessage] = {
    val user = state.userById.get(message.user)
    val name = user.fold("<unknown>")(_.name)
    val replyId = randomReplyId
    val text = s"$name said: ${message.text}"
    Seq(SendMessage(replyId, message.channel, text))
  }

}
