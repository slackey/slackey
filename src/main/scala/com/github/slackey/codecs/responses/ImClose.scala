package com.github.slackey.codecs.responses

case class ImClose(
  no_op: Option[Boolean],
  already_closed: Option[Boolean]
)
