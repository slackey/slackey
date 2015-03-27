package com.nglarry.slacka.codecs.types

object BotMessage {
  def apply(m: Message): BotMessage =
    BotMessage(m.`type`, m.subtype.get, m.channel, m.bot_id.get, m.username.get, m.text.get, m.ts)
}

case class BotMessage(
  `type`: String,
  subtype: String,
  channel: String,
  bot_id: String,
  username: String,
  text: String,
  ts: String
)
