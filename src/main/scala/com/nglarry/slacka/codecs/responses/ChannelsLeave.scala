package com.nglarry.slacka.codecs.responses

import org.json4s._
import com.nglarry.slacka.codecs.types._

case class ChannelsLeave(
  not_in_channel: Option[Boolean]
)
