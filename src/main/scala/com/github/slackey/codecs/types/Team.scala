package com.github.slackey.codecs.types

case class Team(
  id: String,
  name: String,
  email_domain: String,
  domain: String,
  msg_edit_window_mins: Long,
  over_storage_limit: Boolean
)
