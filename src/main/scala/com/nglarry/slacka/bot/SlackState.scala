package com.nglarry.slacka.bot

import org.json4s._
import org.json4s.jackson.JsonMethods._

import com.nglarry.slacka.codecs.extract
import com.nglarry.slacka.codecs.responses.RtmStart
import com.nglarry.slacka.codecs.types._

object SlackState {
  def apply(start: RtmStart): SlackState =
    SlackState(start.self, start.team, start.users, start.channels, start.groups, start.ims)
}

case class SlackState(
    self: Self,
    team: Team,
    users: List[User],
    channels: List[Channel],
    groups: List[Group],
    ims: List[IM]) {
  lazy val userById: Map[String, User] = users.iterator.map(u => u.id -> u).toMap
  lazy val channelById: Map[String, Channel] = channels.iterator.map(c => c.id -> c).toMap
  lazy val groupById: Map[String, Group] = groups.iterator.map(g => g.id -> g).toMap
  lazy val imById: Map[String, IM] = ims.iterator.map(i => i.id -> i).toMap
  lazy val userSelf: User = userById(self.id)
  lazy val channelsIn: List[Channel] = channels.filter(_.is_member == Some(true))

  def update(s: String): SlackState =
    update(parse(s).asInstanceOf[JObject])

  def update(json: JObject): SlackState = {
    val JString(rtmType) = json \ "type"
    rtmType match {
      case "message" =>
        val message = extract[Message](json)
        message.subtype match {
          case Some("channel_join") =>
            updateChannel(message.channel) { oldChannel =>
              oldChannel.copy(members = Some(oldChannel.members.getOrElse(Set.empty) + message.user))
            }
          case Some("channel_leave") =>
            updateChannel(message.channel) { oldChannel =>
              oldChannel.copy(members = Some(oldChannel.members.getOrElse(Set.empty) - message.user))
            }
          case Some("channel_topic") =>
            val newTopic = Topic(message.topic.getOrElse(""), message.user, message.ts.toDouble.toLong)
            updateChannel(message.channel) { _.copy(topic = Some(newTopic)) }
          case Some("channel_purpose") =>
            val newPurpose = Purpose(message.purpose.getOrElse(""), message.user, message.ts.toDouble.toLong)
            updateChannel(message.channel) { _.copy(purpose = Some(newPurpose)) }
          case Some("channel_name") =>
            updateChannel(message.channel) { _.copy(name = message.name.getOrElse("")) }
          case Some("channel_archive") =>
            updateChannel(message.channel) { _.copy(is_archived = Some(true)) }
          case Some("channel_unarchive") =>
            updateChannel(message.channel) { _.copy(is_archived = Some(false)) }
          case _ =>
            this
        }
      case "channel_marked" =>
        (for {
          JObject(fields) <- json
          ("channel", JString(channelId)) <- fields
          ("ts", JString(ts)) <- fields
        } yield updateChannel(channelId) { _.copy(last_read = Some(ts)) }).head
      case "channel_created" =>
        val channel = extract[Channel](json \ "channel")
        if (channelById.contains(channel.id)) this else copy(channels = channel +: channels)
      case "channel_joined" =>
        val channel = extract[Channel](json \ "channel")
        channelById.get(channel.id) match {
          case Some(oldChannel) =>
            copy(channels = channels.map(c => if (c.id == channel.id) channel else c))
          case None =>
            copy(channels = channel +: channels)
        }
      case "channel_left" =>
        val JString(channelId) = json \ "channel"
        updateChannel(channelId) { _.copy(is_member = Some(false)) }
      case "channel_deleted" =>
        val JString(channelId) = json \ "channel"
        copy(channels = channels.filter(_.id != channelId))
      case "channel_rename" =>
        val channel = extract[Channel](json \ "channel")
        updateChannel(channel.id) { _.copy(name = channel.name) }
      case "channel_archive" =>
        val JString(channelId) = json \ "channel"
        updateChannel(channelId) { _.copy(is_archived = Some(true)) }
      case "channel_unarchive" =>
        val JString(channelId) = json \ "channel"
        updateChannel(channelId) { _.copy(is_archived = Some(false)) }
      case "im_created" =>
        val im = extract[IM](json \ "channel")
        copy(ims = im +: ims)
      case "im_open" =>
        val JString(imId) = json \ "channel"
        updateIM(imId) { _.copy(is_open = Some(true)) }
      case "im_close" =>
        val JString(imId) = json \ "channel"
        updateIM(imId) { _.copy(is_open = Some(false)) }
      case "im_marked" =>
        (for {
          JObject(fields) <- json
          ("channel", JString(imId)) <- fields
          ("ts", JString(ts)) <- fields
        } yield updateIM(imId) { _.copy(last_read = Some(ts)) }).head
      case "group_joined" =>
        val group = extract[Group](json \ "channel")
          .copy(is_open = Some(true))  // assumed
        copy(groups = group +: groups)
      case "group_left" =>
        val JString(groupId) = json \ "channel"
        copy(groups = groups.filter(_.id != groupId))
      case "group_open" =>
        val JString(groupId) = json \ "channel"
        updateGroup(groupId) { _.copy(is_open = Some(true)) }
      case "group_close" =>
        val JString(groupId) = json \ "channel"
        updateGroup(groupId) { _.copy(is_open = Some(false)) }
      case "group_rename" =>
        val group = extract[Group](json \ "channel")
        updateGroup(group.id) { oldGroup => oldGroup.copy(name = group.name) }
      case "group_archive" =>
        val JString(groupId) = json \ "channel"
        updateGroup(groupId) { oldGroup =>
          oldGroup.copy(is_archived = Some(true))
        }
      case "group_unarchive" =>
        val JString(groupId) = json \ "channel"
        updateGroup(groupId) { oldGroup =>
          oldGroup.copy(is_archived = Some(false))
        }
      case "group_marked" =>
        (for {
          JObject(fields) <- json
          ("channel", JString(groupId)) <- fields
          ("ts", JString(ts)) <- fields
        } yield updateGroup(groupId) { _.copy(last_read = Some(ts)) }).head
      case "presence_change" =>
        (for {
          JObject(fields) <- json
          ("user", JString(userId)) <- fields
          ("presence", JString(presence)) <- fields
        } yield updateUser(userId) { _.copy(presence = Some(presence)) }).head
      case "manual_presence_change" =>
        val JString(presence) = json \ "presence"
        copy(self = self.copy(manual_presence = presence))
      case "user_change" =>
        val user = extract[User](json \ "user")
        val state = updateUser(user.id) { oldUser => user}
        if (user.deleted)
          state.copy(ims = state.ims.map(i => if (i.user == user.id) i.copy(is_user_deleted = Some(true)) else i))
        else
          state
      case "team_join" =>
        val user = extract[User](json \ "user")
        if (userById.contains(user.id)) this else copy(users = user +: users)
      case "team_rename" =>
        val JString(name) = json \ "name"
        copy(team = team.copy(name = name))
      case "team_domain_change" =>
        val JString(domain) = json \ "domain"
        copy(team = team.copy(domain = domain))
      case _ =>
        this
    }
  }

  def updateChannel(channelId: String)(update: Channel => Channel): SlackState =
    copy(channels = channels.map(c => if (c.id == channelId) update(c) else c))

  def updateGroup(groupId: String)(update: Group => Group): SlackState =
    copy(groups = groups.map(g => if (g.id == groupId) update(g) else g))

  def updateIM(imId: String)(update: IM => IM): SlackState =
    copy(ims = ims.map(i => if (i.id == imId) update(i) else i))

  def updateUser(userId: String)(update: User => User): SlackState =
    copy(users = this.users.map(u => if (u.id == userId) update(u) else u))

}
