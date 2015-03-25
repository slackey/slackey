package com.nglarry.slacka.codecs.responses

import org.json4s._
import com.nglarry.slacka.codecs.types._

case class GroupsHistory(
  latest: String,
  messages: List[Message],
  has_more: Boolean
)
