package com.github.slackey.codecs.responses

import com.github.slackey.codecs.types._

case class ImHistory(
  latest: String,
  messages: List[Message],
  has_more: Boolean
)
