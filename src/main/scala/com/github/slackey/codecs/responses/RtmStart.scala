package com.github.slackey.codecs.responses

import com.github.slackey.codecs.types._

case class RtmStart(
  url: String,
  self: Self,
  team: Team,
  users: List[User],
  channels: List[Channel],
  groups: List[Group],
  ims: List[IM]
)
