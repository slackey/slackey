package com.nglarry.slacka.codecs.types

case class Group(
  id: String,
  name: String,
  is_group: Boolean,
  created: Long,
  creator: String,
  is_archived: Boolean,
  members: Set[String],
  topic: Topic,
  purpose: Option[Purpose],
  last_read: Option[String],
  latest: Option[Message],
  unread_count: Option[Long],
  unread_count_display: Option[Long],
  is_open: Option[Boolean]
)
