package com.nglarry.slacka.codecs.responses

import org.json4s._
import com.nglarry.slacka.codecs.types._

case class AuthTest(
  url: String,
  team: String,
  user: String,
  team_id: String,
  user_id: String
)
