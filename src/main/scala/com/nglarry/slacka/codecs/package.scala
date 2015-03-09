package com.nglarry.slacka

import org.json4s._
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization

package object codecs {
  implicit val formats = Serialization.formats(NoTypeHints)

  def extract[T](s: String)(implicit m: Manifest[T]): T =
    extract[T](parse(s))

  def extract[T](json: JValue)(implicit m: Manifest[T]): T =
    json.extract[T]

  def isHello(json: JObject) = json \ "type" match {
    case JString("hello") => true
    case _ => false
  }

  def isReply(json: JObject) =
    (json \ "reply_to") != JNothing

}
