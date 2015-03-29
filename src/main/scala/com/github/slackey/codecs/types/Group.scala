package com.github.slackey.codecs.types

case class Group(
  id: String,
  name: String,
  created: Option[Long],
  creator: Option[String],
  is_archived: Option[Boolean],
  members: Option[Set[String]],
  topic: Option[Topic],
  purpose: Option[Purpose],
  last_read: Option[String],
  latest: Option[Message],
  unread_count: Option[Long],
  unread_count_display: Option[Long],
  is_open: Option[Boolean]
)
