package com.nglarry.slacka.codecs.responses

import org.json4s._
import com.nglarry.slacka.codecs.types._

case class GroupsOpen(
  no_op: Option[Boolean],
  already_open: Option[Boolean]
)
