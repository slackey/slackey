package com.nglarry.slacka.codecs.types

case class IM(
  id: String,
  user: String,
  created: Option[Long],
  is_user_deleted: Option[Boolean],
  is_open: Option[Boolean],
  last_read: Option[String],
  unread_count: Option[Long],
  latest: Option[Message]
)
