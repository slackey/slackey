package com.github.slackey

import org.json4s._
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization

/**
 * Provides case class representations of JSON objects used in the Slack API,
 * as well as some utility methods.
 */
package object codecs {
  private implicit val formats = Serialization.formats(NoTypeHints)

  /**
   * Deserializes an instance of T from a JSON string.
   */
  def extract[T](s: String)(implicit m: Manifest[T]): T =
    extract[T](parse(s))

  /**
   * Deserializes an instance of T from a json4s JValue.
   */
  def extract[T](json: JValue)(implicit m: Manifest[T]): T =
    json.extract[T]

  /**
   * Returns `true` if a JSON object is a Slack "hello" message.
   */
  def isHello(json: JObject) = json \ "type" match {
    case JString("hello") => true
    case _ => false
  }

  /**
   * Returns `true` if a JSON object is a Slack "reply" message.
   */
  def isReply(json: JObject) =
    (json \ "reply_to") != JNothing

}
