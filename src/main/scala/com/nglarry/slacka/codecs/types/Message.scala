package com.nglarry.slacka.codecs.types

case class Message(
  `type`: String,
  channel: String,
  user: String,
  text: String,
  ts: String,
  subtype: Option[String],
  hidden: Option[Boolean],
  topic: Option[String],
  purpose: Option[String],
  old_name: Option[String],
  name: Option[String],
  edited: Option[Edited]
)
