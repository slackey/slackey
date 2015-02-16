package slacka

import com.nglarry.slacka.bot.SlackState
import com.nglarry.slacka.codecs.extract
import com.nglarry.slacka.codecs.types._
import org.scalatest.FunSuite

trait SlackaSuite extends FunSuite {
  val testUser = extract[User](
    s"""
       |{
       |    "id": "U023BECGF",
       |    "name": "bobby",
       |    "deleted": false,
       |    "color": "9f69e7",
       |    "profile": {
       |        "first_name": "Bobby",
       |        "last_name": "Tables",
       |        "real_name": "Bobby Tables",
       |        "email": "bobby@slack.com",
       |        "skype": "my-skype-name",
       |        "phone": "+1 (123) 456 7890",
       |        "image_24": "https://example.com/img24.png",
       |        "image_32": "https://example.com/img32.png",
       |        "image_48": "https://example.com/img48.png",
       |        "image_72": "https://example.com/img72.png",
       |        "image_192": "https://example.com/img192.png"
       |    },
       |    "is_admin": true,
       |    "is_owner": true,
       |    "is_primary_owner": true,
       |    "is_restricted": false,
       |    "is_ultra_restricted": false,
       |    "has_files": true
       |}
     """.stripMargin
  )

  val testSelf = extract[Self](
    s"""
       |{
       |    "id": "${testUser.id}",
       |    "name": "${testUser.name}",
       |    "created": 1402463766,
       |    "manual_presence": "active"
       |}
     """.stripMargin
  )

  val testTeam = extract[Team](
    s"""
       |{
       |    "id": "T024BE7LD",
       |    "name": "Example Team",
       |    "email_domain": "",
       |    "domain": "example",
       |    "msg_edit_window_mins": -1,
       |    "over_storage_limit": false
       |}
     """.stripMargin
  )

  val testChannel = extract[Channel](
    s"""
       |{
       |    "id": "C024BE91L",
       |    "name": "fun",
       |    "is_channel": true,
       |    "created": 1360782804,
       |    "creator": "U024BE7LH",
       |    "is_archived": false,
       |    "is_general": false,
       |    "members": [
       |        "U024BE7LH"
       |    ],
       |    "topic": {
       |        "value": "Fun times",
       |        "creator": "U024BE7LV",
       |        "last_set": 1369677212
       |    },
       |    "purpose": {
       |        "value": "This channel is for fun",
       |        "creator": "U024BE7LH",
       |        "last_set": 1360782804
       |    },
       |    "is_member": true,
       |    "last_read": "1401383885.000061",
       |    "latest": {
       |        "type": "message",
       |        "channel": "C024BE91L",
       |        "user": "U024BE7LH",
       |        "text": "Hello world",
       |        "ts": "1355517523.000005"
       |    },
       |    "unread_count": 0,
       |    "unread_count_display": 0
       |}
     """.stripMargin
  )

  val testIM = extract[IM](
    s"""
       |{
       |    "id": "D024BFF1M",
       |    "is_im": true,
       |    "user": "U024BE7LH",
       |    "created": 1360782804,
       |    "is_user_deleted": false
       |}
     """.stripMargin
  )

  val testGroup = extract[Group](
    s"""
       |{
       |    "id": "G024BE91L",
       |    "name": "secretplans",
       |    "is_group": true,
       |    "created": 1360782804,
       |    "creator": "U024BE7LH",
       |    "is_archived": false,
       |    "members": [
       |        "U024BE7LH"
       |    ],
       |    "topic": {
       |        "value": "Secret plans on hold",
       |        "creator": "U024BE7LV",
       |        "last_set": 1369677212
       |    },
       |    "purpose": {
       |        "value": "Discuss secret plans that no-one else should know",
       |        "creator": "U024BE7LH",
       |        "last_set": 1360782804
       |    },
       |    "last_read": "1401383885.000061",
       |    "latest": {
       |        "type": "message",
       |        "channel": "G024BE91L",
       |        "user": "U024BE7LH",
       |        "text": "Hello world",
       |        "ts": "1355517523.000005"
       |    },
       |    "unread_count": 0,
       |    "unread_count_display": 0
       |
       |}
     """.stripMargin
  )

  val testMessage = testChannel.latest.get

  val testProfile = testUser.profile

  val testState = SlackState(testSelf, testTeam, List(testUser), List(testChannel), List(testGroup), List(testIM))
}