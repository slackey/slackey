package com.nglarry.slacka.codecs.responses

import org.json4s._
import com.nglarry.slacka.codecs.types._

case class UsersGetPresence(
  presence: String,
  online: Option[Boolean],
  auto_away: Option[Boolean],
  manual_away: Option[Boolean],
  connection_count: Option[Int],
  last_activity: Option[Long]
)
