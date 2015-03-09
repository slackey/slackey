package slacka.bot

import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import slacka.SlackaSuite

class SlackStateSpec extends SlackaSuite {
  implicit val formats = Serialization.formats(NoTypeHints)

  test("test preconditions are met") {
    assert(testState.channelById.get(testChannel.id).isDefined)
    assert(testState.groupById.get(testGroup.id).isDefined)
    assert(testState.userById.get(testUser.id).isDefined)
  }

  test("updateChannel is correct") {
    val newName = testChannel.name + "foo"
    val newState = testState.updateChannel(testChannel.id) { c =>
      c.copy(name = newName)
    }
    assert(newState.channelById(testChannel.id).name === newName)
  }

  test("updateGroup is correct") {
    val newName = testGroup.name + "foo"
    val newState = testState.updateGroup(testGroup.id) { g =>
      g.copy(name = newName)
    }
    assert(newState.groupById(testGroup.id).name === newName)
  }

  test("updateUser is correct") {
    val newName = testUser.name + "foo"
    val newState = testState.updateUser(testUser.id) { u =>
      u.copy(name = newName)
    }
    assert(newState.userById(testUser.id).name === newName)
  }

  test("handles channel_marked correctly") {
    val ts = "1401383123.000061"
    val msg =
      s"""
         |{
         |    "type": "channel_marked",
         |    "channel": "${testChannel.id}",
         |    "ts": "$ts"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.channelById(testChannel.id).last_read.get === ts)
  }

  test("handles channel_created correctly") {
    val id = "C999"
    val name = "channel created"
    val msg =
      s"""
         |{
         |    "type": "channel_created",
         |    "channel": {
         |        "id": "$id",
         |        "name": "$name",
         |        "created": 1360782804,
         |        "creator": "U024BE7LH"
         |    }
         |}
       """.stripMargin
    assert(testState.channelById.get(id).isEmpty)
    val newState = testState.update(msg)
    assert(newState.channelById(id).name === name)
  }

  test("handles channel_join correctly") {
    // test when channel doesn't exist in `channels`
    val newChan = testChannel.copy(id = "C2", name = "chan2")
    val serializedChannel = write(newChan)
    val msg =
      s"""
         |{
         |    "type": "channel_joined",
         |    "channel": $serializedChannel
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.channels.size === testState.channels.size + 1)
    assert(newState.channelById.get(newChan.id).isDefined)

    // leave new chan
    val newState2 = newState.updateChannel(newChan.id) { _.copy(is_member = false) }

    // test when channel exists in `channels` but not a member
    val newState3 = newState2.update(msg)
    assert(newState3.channels.size === newState.channels.size)
    assert(newState3.channelById.get(newChan.id).isDefined)
  }

  test("handles channel_left correctly") {
    assert(testChannel.is_member === true)
    val msg =
      s"""
         |{
         |    "type": "channel_left",
         |    "channel": "${testChannel.id}"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.channelById(testChannel.id).is_member === false)
  }

  test("handles channel_deleted correctly") {
    val msg =
      s"""
         |{
         |    "type": "channel_deleted",
         |    "channel": "${testChannel.id}"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.channelById.get(testChannel.id).isEmpty)
  }

  test("handles channel_rename correctly") {
    val newName = "new name"
    assert(testChannel.name !== newName)
    val msg =
      s"""
         |{
         |    "type": "channel_rename",
         |    "channel": {
         |        "id": "${testChannel.id}",
         |        "name": "$newName",
         |        "created": 1360782804
         |    }
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.channelById(testChannel.id).name === newName)
  }

  test("handles channel_archive correctly") {
    assert(testChannel.is_archived === false)
    val msg =
      s"""
         |{
         |    "type": "channel_archive",
         |    "channel": "${testChannel.id}",
         |    "user": "U024BE7LH"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.channelById(testChannel.id).is_archived === true)
  }

  test("handles channel_unarchive correctly") {
    val state = testState.updateChannel(testChannel.id) { _.copy(is_archived = true) }
    assert(state.channelById(testChannel.id).is_archived === true)
    val msg =
      s"""
         |{
         |    "type": "channel_unarchive",
         |    "channel": "${testChannel.id}",
         |    "user": "U024BE7LH"
         |}
       """.stripMargin
    val newState = state.update(msg)
    assert(newState.channelById(testChannel.id).is_archived === false)
  }

  test("handles im_create correctly") {
    val newIM = testIM.copy(id = "D2")
    val msg =
      s"""
         |{
         |    "type": "im_created",
         |    "user": "U024BE7LH",
         |    "channel": ${write(newIM)}
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.imById.get(newIM.id).isDefined)
  }

  test("handles im_open correctly") {
    val state = testState.updateIM(testIM.id) { _.copy(is_open = Some(false)) }
    val msg =
      s"""
         |{
         |    "type": "im_open",
         |    "user": "U024BE7LH",
         |    "channel": "${testIM.id}"
         |}
       """.stripMargin
    val newState = state.update(msg)
    assert(newState.imById(testIM.id).is_open === Some(true))
  }

  test("handles im_close correctly") {
    val state = testState.updateIM(testIM.id) { _.copy(is_open = Some(true)) }
    val msg =
      s"""
         |{
         |    "type": "im_close",
         |    "user": "U024BE7LH",
         |    "channel": "${testIM.id}"
         |}
       """.stripMargin
    val newState = state.update(msg)
    assert(newState.imById(testIM.id).is_open === Some(false))
  }

  test("handles im_marked correctly") {
    val timestamp = "1401383885.000061"
    assert(testIM.last_read !== Some(timestamp))
    val msg =
      s"""
         |{
         |    "type": "im_marked",
         |    "channel": "${testIM.id}",
         |    "ts": "$timestamp"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.imById(testIM.id).last_read === Some(timestamp))
  }

  test("handles group_joined correctly") {
    val group = testGroup.copy(id = "G123")
    val msg =
      s"""
         |{
         |    "type": "group_joined",
         |    "channel": ${write(group)}
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.groupById.get(group.id).isDefined)
  }

  test("handles group_left correctly") {
    val msg =
      s"""
         |{
         |    "type": "group_left",
         |    "channel": "${testGroup.id}"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.groupById.get(testGroup.id).isEmpty)
  }

  test("handles group_open correctly") {
    assert(testGroup.is_open !== Some(true))
    val msg =
      s"""
         |{
         |    "type": "group_open",
         |    "channel": "${testGroup.id}"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.groupById(testGroup.id).is_open === Some(true))
  }

  test("handles group_close correctly") {
    assert(testGroup.is_open !== Some(false))
    val msg =
      s"""
         |{
         |    "type": "group_close",
         |    "channel": "${testGroup.id}"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.groupById(testGroup.id).is_open === Some(false))
  }

  test("handles group_archive correctly") {
    val state = testState.updateGroup(testGroup.id) { _.copy(is_archived = false) }
    val msg =
      s"""
         |{
         |    "type": "group_archive",
         |    "channel": "${testGroup.id}"
         |}
       """.stripMargin
    val newState = state.update(msg)
    assert(newState.groupById(testGroup.id).is_archived === true)
  }

  test("handles group_unarchive correctly") {
    val state = testState.updateGroup(testGroup.id) { _.copy(is_archived = true) }
    val msg =
      s"""
         |{
         |    "type": "group_unarchive",
         |    "channel": "${testGroup.id}"
         |}
       """.stripMargin
    val newState = state.update(msg)
    assert(newState.groupById(testGroup.id).is_archived === false)
  }

  test("handles group_rename correctly") {
    val name = "new name"
    assert(testGroup.name !== name)
    val msg =
      s"""
         |{
         |    "type": "group_rename",
         |    "channel": {
         |        "id":"${testGroup.id}",
         |        "name":"$name",
         |        "created":1360782804
         |    }
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.groupById(testGroup.id).name === name)
  }

  test("handles group_marked correctly") {
    val ts = "1401383123.000061"
    assert(testGroup.last_read !== Some(ts))
    val msg =
      s"""
         |{
         |    "type": "group_marked",
         |    "channel": "${testGroup.id}",
         |    "ts": "$ts"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.groupById(testGroup.id).last_read === Some(ts))
  }

  test("handles presence_change correctly") {
    val presence = "away"
    assert(testUser.presence !== presence)
    val msg =
      s"""
         |{
         |    "type": "presence_change",
         |    "user": "${testUser.id}",
         |    "presence": "$presence"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.userById(testUser.id).presence === Some(presence))
  }

  test("handles manual_presence_change correctly") {
    val presence = "away"
    assert(testSelf.manual_presence !== presence)
    val msg =
      s"""
         |{
         |    "type": "manual_presence_change",
         |    "presence": "$presence"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.self.manual_presence === presence)
  }

  test("handles user_change (basic) correctly") {
    val newName = "test user"
    assert(testUser.name !== newName)
    val user = testUser.copy(name = newName)
    val msg =
      s"""
         |{
         |    "type": "user_change",
         |    "user": ${write(user)}
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.userById(testUser.id).name === newName)
  }

  test("handles user_change (deleted) correctly") {
    val user = testUser.copy(deleted = true)
    // create an IM with a user that will be deleted
    val im = testIM.copy(id = "D987", user = user.id)
    val state = testState.copy(ims = testState.ims :+ im)
    assert(state.imById.get(im.id).isDefined)
    assert(state.imById(im.id).is_user_deleted !== Some(true))
    val msg =
      s"""
         |{
         |    "type": "user_change",
         |    "user": ${write(user)}
         |}
       """.stripMargin
    val newState = state.update(msg)
    assert(newState.imById(im.id).is_user_deleted === Some(true))
  }

  test("handles team_join correctly") {
    val newUser = testUser.copy(id = "U456")
    val msg =
      s"""
         |{
         |    "type": "team_join",
         |    "user": ${write(newUser)}
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.users.size === testState.users.size + 1)
    assert(newState.userById.get(newUser.id).isDefined)
  }

  test("handles team_rename correctly") {
    val newName = "test name"
    assert(testState.team.name !== newName)
    val msg =
      s"""
         |{
         |    "type": "team_rename",
         |    "name": "$newName"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.team.name === newName)
  }

  test("handles team_domain_change correctly") {
    val newDomain = "foobarbaz"
    assert(testState.team.domain !== newDomain)
    val msg =
      s"""
         |{
         |    "type": "team_domain_change",
         |    "url": "https://$newDomain.slack.com",
         |    "domain": "$newDomain"
         |}
       """.stripMargin
    val newState = testState.update(msg)
    assert(newState.team.domain === newDomain)
  }

}
