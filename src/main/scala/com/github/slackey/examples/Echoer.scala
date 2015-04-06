package com.github.slackey.examples

import scala.util.Random

import org.json4s._

import com.github.slackey.bot.{RealTimeMessagingListener, SendMessage, SlackState}
import com.github.slackey.codecs.types.SimpleMessage

/**
 * An example message listener that echos basic messages along with the user's name.
 */
object Echoer extends RealTimeMessagingListener {
  override def onSimpleMessage(state: SlackState, message: SimpleMessage, json: JObject): Seq[SendMessage] = {
    val user = state.userById.get(message.user)
    val name = user.fold("<unknown>")(_.name)
    val replyId = Random.nextLong()
    val text = s"$name said: ${message.text}"
    Seq(SendMessage(replyId, message.channel, text))
  }

}
