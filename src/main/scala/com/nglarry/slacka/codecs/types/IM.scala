package com.nglarry.slacka.codecs.types

case class IM(
  id: String,
  is_im: Boolean,
  user: String,
  created: Long,
  is_user_deleted: Option[Boolean],
  is_open: Option[Boolean],
  last_read: Option[String],
  unread_count: Option[Long],
  latest: Option[Message]
)
