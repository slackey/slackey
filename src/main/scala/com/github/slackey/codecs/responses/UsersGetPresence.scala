package com.github.slackey.codecs.responses

case class UsersGetPresence(
  presence: String,
  online: Option[Boolean],
  auto_away: Option[Boolean],
  manual_away: Option[Boolean],
  connection_count: Option[Int],
  last_activity: Option[Long]
)
