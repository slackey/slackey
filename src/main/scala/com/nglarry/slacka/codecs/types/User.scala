package com.nglarry.slacka.codecs.types

case class User(
  id: String,
  name: String,
  deleted: Boolean,
  color: Option[String],
  profile: Profile,
  is_admin: Option[Boolean],
  is_owner: Option[Boolean],
  is_primary_owner: Option[Boolean],
  is_restricted: Option[Boolean],
  is_ultra_restricted: Option[Boolean],
  is_bot: Option[Boolean],
  has_files: Option[Boolean],
  presence: Option[String]
)
