package com.nglarry.slacka.api

import com.nglarry.slacka.codecs._
import com.nglarry.slacka.codecs.responses._
import com.ning.http.client._
import org.json4s._
import org.json4s.jackson.JsonMethods._

object SlackWebApi {
  def defaultHttpClientConfig =
    new AsyncHttpClientConfig.Builder().build()

  def apply(token: String, clientConfig: AsyncHttpClientConfig = defaultHttpClientConfig): SlackWebApi = {
    new SlackWebApi(token, clientConfig)
  }

  private def url(method: String): String = s"https://slack.com/api/$method"

  private def defaultHandler[T] = new SlackResponseHandler[T] {}
}

class SlackWebApi(
    token: String,
    clientConfig: AsyncHttpClientConfig = SlackWebApi.defaultHttpClientConfig) {
  import SlackWebApi._

  val client: AsyncHttpClient = new AsyncHttpClient(clientConfig)

  def isOpen: Boolean = !isClosed

  def isClosed: Boolean = client.isClosed

  def close(): Unit = {
    if (!isClosed) client.close()
  }

  object channels {
    def info(channel: String, handler: SlackResponseHandler[ChannelsInfo] = defaultHandler) =
      request[ChannelsInfo]("channels.info", Map("channel" -> channel), handler = handler)
  }

  object rtm {
    def start(handler: SlackResponseHandler[RtmStart] = defaultHandler) =
      request[RtmStart]("rtm.start", Map.empty, handler = handler)
  }

  def request[T](
      method: String,
      args: Map[String, Object],
      handler: SlackResponseHandler[T] = defaultHandler)
      (implicit m: Manifest[T]): ListenableFuture[SlackResponse[T]] = {
    val req = (args + ("token" -> token)).foldLeft(client.preparePost(url(method))) { case (r, (k, v)) =>
      r.addFormParam(k, v.toString)
    }
    val completionHandler = new AsyncCompletionHandler[SlackResponse[T]] {
      override def onCompleted(response: Response) = {
        // println(response.getResponseBody)
        val json = parse(response.getResponseBody)
        val JBool(ok) = json \\ "ok"
        if (!ok) {
          val JString(error) = json \\ "error"
          handler.onSlackError(error)
          Left(error)
        } else {
          val result = extract[T](json)
          handler.onSuccess(result)
          Right(result)
        }
      }
      override def onThrowable(t: Throwable): Unit = handler.onThrowable(t)
    }
    req.execute(completionHandler)
  }

}
