package com.github.slackey.codecs.types

object SimpleMessage {
  def apply(m: Message): SimpleMessage =
    SimpleMessage(m.`type`, m.channel, m.user.get, m.text.get, m.ts)
}

case class SimpleMessage(
  `type`: String,
  channel: String,
  user: String,
  text: String,
  ts: String
)
