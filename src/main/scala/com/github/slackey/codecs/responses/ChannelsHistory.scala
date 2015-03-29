package com.github.slackey.codecs.responses

import com.github.slackey.codecs.types._

case class ChannelsHistory(
  latest: String,
  messages: List[Message],
  has_more: Boolean
)
