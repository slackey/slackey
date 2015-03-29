package com.github.slackey.codecs.types

case class Profile(
  bot_id: Option[String],
  first_name: Option[String],
  last_name: Option[String],
  real_name: Option[String],
  real_name_normalized: Option[String],
  email: Option[String],
  skype: Option[String],
  phone: Option[String],
  image_24: String,
  image_32: String,
  image_48: String,
  image_72: String,
  image_192: String
)
