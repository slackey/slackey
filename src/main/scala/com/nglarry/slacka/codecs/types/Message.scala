package com.nglarry.slacka.codecs.types

case class Message(
  `type`: String,
  channel: String,
  ts: String,
  user: Option[String],
  bot_id: Option[String],
  username: Option[String],
  text: Option[String],
  subtype: Option[String],
  hidden: Option[Boolean],
  topic: Option[String],
  purpose: Option[String],
  old_name: Option[String],
  name: Option[String],
  message: Option[Message],
  edited: Option[Edited]
)
