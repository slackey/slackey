package com.nglarry.slacka.codecs.responses

import org.json4s._
import com.nglarry.slacka.codecs.types._

case class ImClose(
  no_op: Option[Boolean],
  already_closed: Option[Boolean]
)
