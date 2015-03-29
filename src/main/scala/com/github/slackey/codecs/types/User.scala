package com.github.slackey.codecs.types

case class User(
  id: String,
  name: String,
  deleted: Boolean,
  profile: Profile,
  color: Option[String],
  is_admin: Option[Boolean],
  is_owner: Option[Boolean],
  is_primary_owner: Option[Boolean],
  is_restricted: Option[Boolean],
  is_ultra_restricted: Option[Boolean],
  is_bot: Option[Boolean],
  has_files: Option[Boolean],
  presence: Option[String]
)
