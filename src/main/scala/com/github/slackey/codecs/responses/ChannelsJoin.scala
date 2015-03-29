package com.github.slackey.codecs.responses

import com.github.slackey.codecs.types._

case class ChannelsJoin(
  channel: Channel,
  already_in_channel: Option[Boolean]
)
