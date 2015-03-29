package com.github.slackey.codecs.types

object MeMessage {
  def apply(m: Message): MeMessage =
    MeMessage(m.`type`, m.subtype.get, m.channel, m.user.get, m.text.get, m.ts)
}

case class MeMessage(
  `type`: String,
  subtype: String,
  channel: String,
  user: String,
  text: String,
  ts: String
)
