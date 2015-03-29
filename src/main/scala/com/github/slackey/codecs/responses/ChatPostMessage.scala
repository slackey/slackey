package com.github.slackey.codecs.responses

import com.github.slackey.codecs.types._

case class ChatPostMessage(
  ts: String,
  channel: String,
  message: Message
)
