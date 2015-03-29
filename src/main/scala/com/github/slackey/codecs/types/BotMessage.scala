package com.github.slackey.codecs.types

object BotMessage {
  def apply(m: Message): BotMessage =
    BotMessage(m.`type`, m.subtype.get, m.channel, m.text.get, m.bot_id, m.username, m.ts)
}

case class BotMessage(
  `type`: String,
  subtype: String,
  channel: String,
  text: String,
  username: Option[String],
  bot_id: Option[String],
  ts: String
)
