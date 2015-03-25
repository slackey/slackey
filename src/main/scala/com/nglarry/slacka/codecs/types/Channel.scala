package com.nglarry.slacka.codecs.types

case class Channel(
  id: String,
  name: String,
  created: Option[Long],
  creator: Option[String],
  is_archived: Option[Boolean],
  is_general: Option[Boolean],
  members: Option[Set[String]],
  is_member: Option[Boolean],
  topic: Option[Topic] = None,
  purpose: Option[Purpose] = None,
  last_read: Option[String] = None,
  latest: Option[Message] = None,
  unread_count: Option[Long] = None,
  unread_count_display: Option[Long] = None
)
