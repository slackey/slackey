package com.nglarry.slacka.codecs.types

case class Channel(
  id: String,
  name: String,
  is_channel: Boolean,
  created: Long,
  creator: String,
  is_archived: Boolean,
  is_general: Boolean,
  members: Set[String],
  is_member: Boolean,
  topic: Option[Topic] = None,
  purpose: Option[Purpose] = None,
  last_read: Option[String] = None,
  latest: Option[Message] = None,
  unread_count: Option[Long] = None,
  unread_count_display: Option[Long] = None
)
