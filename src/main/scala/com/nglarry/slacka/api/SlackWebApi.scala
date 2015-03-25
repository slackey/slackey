package com.nglarry.slacka.api

import com.ning.http.client._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import com.nglarry.slacka.codecs._
import com.nglarry.slacka.codecs.responses._

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
  import com.nglarry.slacka.api.SlackWebApi._

  val client: AsyncHttpClient = new AsyncHttpClient(clientConfig)

  def isOpen: Boolean = !isClosed

  def isClosed: Boolean = client.isClosed

  def close(): Unit = {
    if (!isClosed) client.close()
  }

  //{{{
  object auth {
    def test(handler: SlackResponseHandler[AuthTest] = defaultHandler) =
      request[AuthTest]("auth.test", Map.empty, handler = handler)
  }

  object channels {
    def archive(channel: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("channels.archive", Map("channel" -> channel), handler = handler)
    def create(name: String, handler: SlackResponseHandler[ChannelsCreate] = defaultHandler) =
      request[ChannelsCreate]("channels.create", Map("name" -> name), handler = handler)
    def history(channel: String, latest: Option[String] = None, oldest: Option[String] = None, inclusive: Option[Boolean] = None, count: Option[Int] = None, handler: SlackResponseHandler[ChannelsHistory] = defaultHandler) =
      request[ChannelsHistory]("channels.history", Map("channel" -> channel, "latest" -> latest, "oldest" -> oldest, "inclusive" -> inclusive, "count" -> count), handler = handler)
    def info(channel: String, handler: SlackResponseHandler[ChannelsInfo] = defaultHandler) =
      request[ChannelsInfo]("channels.info", Map("channel" -> channel), handler = handler)
    def invite(channel: String, user: String, handler: SlackResponseHandler[ChannelsInvite] = defaultHandler) =
      request[ChannelsInvite]("channels.invite", Map("channel" -> channel, "user" -> user), handler = handler)
    def join(name: String, handler: SlackResponseHandler[ChannelsJoin] = defaultHandler) =
      request[ChannelsJoin]("channels.join", Map("name" -> name), handler = handler)
    def kick(channel: String, user: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("channels.kick", Map("channel" -> channel, "user" -> user), handler = handler)
    def leave(channel: String, handler: SlackResponseHandler[ChannelsLeave] = defaultHandler) =
      request[ChannelsLeave]("channels.leave", Map("channel" -> channel), handler = handler)
    def list(exclude_archived: Option[String] = None, handler: SlackResponseHandler[ChannelsList] = defaultHandler) =
      request[ChannelsList]("channels.list", Map("exclude_archived" -> exclude_archived), handler = handler)
    def mark(channel: String, ts: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("channels.mark", Map("channel" -> channel, "ts" -> ts), handler = handler)
    def rename(channel: String, name: String, handler: SlackResponseHandler[ChannelsRename] = defaultHandler) =
      request[ChannelsRename]("channels.rename", Map("channel" -> channel, "name" -> name), handler = handler)
    def setPurpose(channel: String, purpose: String, handler: SlackResponseHandler[ChannelsSetPurpose] = defaultHandler) =
      request[ChannelsSetPurpose]("channels.setPurpose", Map("channel" -> channel, "purpose" -> purpose), handler = handler)
    def setTopic(channel: String, topic: String, handler: SlackResponseHandler[ChannelsSetTopic] = defaultHandler) =
      request[ChannelsSetTopic]("channels.setTopic", Map("channel" -> channel, "topic" -> topic), handler = handler)
    def unarchive(channel: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("channels.unarchive", Map("channel" -> channel), handler = handler)
  }

  object chat {
    def delete(ts: String, channel: String, handler: SlackResponseHandler[ChatDelete] = defaultHandler) =
      request[ChatDelete]("chat.delete", Map("ts" -> ts, "channel" -> channel), handler = handler)
    def postMessage(channel: String, text: String, username: Option[String] = None, as_user: Option[Boolean] = None, parse: Option[String] = None, link_names: Option[Boolean] = None, attachments: Option[String] = None, unfurl_links: Option[Boolean] = None, unfurl_media: Option[Boolean] = None, icon_url: Option[String] = None, icon_emoji: Option[String] = None, handler: SlackResponseHandler[ChatPostMessage] = defaultHandler) =
      request[ChatPostMessage]("chat.postMessage", Map("channel" -> channel, "text" -> text, "username" -> username, "as_user" -> as_user, "parse" -> parse, "link_names" -> link_names, "attachments" -> attachments, "unfurl_links" -> unfurl_links, "unfurl_media" -> unfurl_media, "icon_url" -> icon_url, "icon_emoji" -> icon_emoji), handler = handler)
    def update(ts: String, channel: String, text: String, handler: SlackResponseHandler[ChatUpdate] = defaultHandler) =
      request[ChatUpdate]("chat.update", Map("ts" -> ts, "channel" -> channel, "text" -> text), handler = handler)
  }

  object emoji {
    def list(handler: SlackResponseHandler[EmojiList] = defaultHandler) =
      request[EmojiList]("emoji.list", Map.empty, handler = handler)
  }

  object groups {
    def archive(channel: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("groups.archive", Map("channel" -> channel), handler = handler)
    def close(channel: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("groups.close", Map("channel" -> channel), handler = handler)
    def create(name: String, handler: SlackResponseHandler[GroupsCreate] = defaultHandler) =
      request[GroupsCreate]("groups.create", Map("name" -> name), handler = handler)
    def createChild(channel: String, handler: SlackResponseHandler[GroupsCreateChild] = defaultHandler) =
      request[GroupsCreateChild]("groups.createChild", Map("channel" -> channel), handler = handler)
    def history(channel: String, latest: Option[String] = None, oldest: Option[String] = None, inclusive: Option[Boolean] = None, count: Option[Int] = None, handler: SlackResponseHandler[GroupsHistory] = defaultHandler) =
      request[GroupsHistory]("groups.history", Map("channel" -> channel, "latest" -> latest, "oldest" -> oldest, "inclusive" -> inclusive, "count" -> count), handler = handler)
    def invite(channel: String, user: String, handler: SlackResponseHandler[GroupsInvite] = defaultHandler) =
      request[GroupsInvite]("groups.invite", Map("channel" -> channel, "user" -> user), handler = handler)
    def kick(channel: String, user: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("groups.kick", Map("channel" -> channel, "user" -> user), handler = handler)
    def leave(channel: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("groups.leave", Map("channel" -> channel), handler = handler)
    def list(exclude_archived: Option[String] = None, handler: SlackResponseHandler[GroupsList] = defaultHandler) =
      request[GroupsList]("groups.list", Map("exclude_archived" -> exclude_archived), handler = handler)
    def mark(channel: String, ts: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("groups.mark", Map("channel" -> channel, "ts" -> ts), handler = handler)
    def open(channel: String, handler: SlackResponseHandler[GroupsOpen] = defaultHandler) =
      request[GroupsOpen]("groups.open", Map("channel" -> channel), handler = handler)
    def rename(channel: String, name: String, handler: SlackResponseHandler[GroupsRename] = defaultHandler) =
      request[GroupsRename]("groups.rename", Map("channel" -> channel, "name" -> name), handler = handler)
    def setPurpose(channel: String, purpose: String, handler: SlackResponseHandler[GroupsSetPurpose] = defaultHandler) =
      request[GroupsSetPurpose]("groups.setPurpose", Map("channel" -> channel, "purpose" -> purpose), handler = handler)
    def setTopic(channel: String, topic: String, handler: SlackResponseHandler[GroupsSetTopic] = defaultHandler) =
      request[GroupsSetTopic]("groups.setTopic", Map("channel" -> channel, "topic" -> topic), handler = handler)
    def unarchive(channel: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("groups.unarchive", Map("channel" -> channel), handler = handler)
  }

  object im {
    def close(channel: String, handler: SlackResponseHandler[ImClose] = defaultHandler) =
      request[ImClose]("im.close", Map("channel" -> channel), handler = handler)
    def history(channel: String, latest: Option[String] = None, oldest: Option[String] = None, inclusive: Option[Boolean] = None, count: Option[Int] = None, handler: SlackResponseHandler[ImHistory] = defaultHandler) =
      request[ImHistory]("im.history", Map("channel" -> channel, "latest" -> latest, "oldest" -> oldest, "inclusive" -> inclusive, "count" -> count), handler = handler)
    def list(handler: SlackResponseHandler[ImList] = defaultHandler) =
      request[ImList]("im.list", Map.empty, handler = handler)
    def mark(channel: String, ts: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("im.mark", Map("channel" -> channel, "ts" -> ts), handler = handler)
    def open(user: String, handler: SlackResponseHandler[ImOpen] = defaultHandler) =
      request[ImOpen]("im.open", Map("user" -> user), handler = handler)
  }

  object rtm {
    def start(handler: SlackResponseHandler[RtmStart] = defaultHandler) =
      request[RtmStart]("rtm.start", Map.empty, handler = handler)
  }

  object users {
    def getPresence(user: String, handler: SlackResponseHandler[UsersGetPresence] = defaultHandler) =
      request[UsersGetPresence]("users.getPresence", Map("user" -> user), handler = handler)
    def info(user: String, handler: SlackResponseHandler[UsersInfo] = defaultHandler) =
      request[UsersInfo]("users.info", Map("user" -> user), handler = handler)
    def list(handler: SlackResponseHandler[UsersList] = defaultHandler) =
      request[UsersList]("users.list", Map.empty, handler = handler)
    def setActive(handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("users.setActive", Map.empty, handler = handler)
    def setPresence(presence: String, handler: SlackResponseHandler[Empty] = defaultHandler) =
      request[Empty]("users.setPresence", Map("presence" -> presence), handler = handler)
  }

  //}}}

  def request[T](
      method: String,
      args: Map[String, Any],
      handler: SlackResponseHandler[T] = defaultHandler)
      (implicit m: Manifest[T]): ListenableFuture[SlackResponse[T]] = {
    val req = (args + ("token" -> token)).foldLeft(client.preparePost(url(method))) { case (r, (k, v)) =>
      v match {
        case Some(item) => r.addFormParam(k, item.toString)
        case None => r
        case _ => r.addFormParam(k, v.toString)
      }
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
