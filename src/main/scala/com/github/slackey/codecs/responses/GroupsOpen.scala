package com.github.slackey.codecs.responses

case class GroupsOpen(
  no_op: Option[Boolean],
  already_open: Option[Boolean]
)
