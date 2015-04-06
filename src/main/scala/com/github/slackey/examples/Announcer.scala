package com.github.slackey.examples

import scala.util.Random

import org.json4s._

import com.github.slackey.bot.messages.SendMessage
import com.github.slackey.bot.{RealTimeMessagingListener, SlackState}
import com.github.slackey.codecs.types.SimpleMessage

/**
 * An example listener that allows users to broadcast a message to every room
 * the bot is in by saying '!announce <message>'.
 */
object Announcer extends RealTimeMessagingListener {
  val command = """!announce (.+)""".r

  override def onSimpleMessage(state: SlackState, message: SimpleMessage, json: JObject): Seq[SendMessage] = {
    message.text match {
      case command(msg) =>
        val user = state.userById.get(message.user)
        val name = user.fold("<unknown>")(_.name)
        val text = s"$name announced: $msg"
        state.channelsIn.filter(_.id != message.channel).map { channel =>
          val replyId = Random.nextLong()
          SendMessage(replyId, channel.id, text)
        }
      case _ =>
        Seq.empty
    }
  }

}
