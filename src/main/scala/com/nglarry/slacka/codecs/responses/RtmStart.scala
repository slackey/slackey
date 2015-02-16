package com.nglarry.slacka.codecs.responses

import com.nglarry.slacka.codecs.types._

case class RtmStart(
  url: String,
  self: Self,
  team: Team,
  users: List[User],
  channels: List[Channel],
  groups: List[Group],
  ims: List[IM]
)
