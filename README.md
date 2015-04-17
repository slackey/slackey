# slackey
Slackey is a library for using [Slack's Real Time Messaging API](https://api.slack.com/rtm).  Also included is an interface for the [Web API](https://api.slack.com/web), but it's mostly about making your own Slack lackey (read: bot).

Slackey is built using [Akka actors](http://doc.akka.io/docs/akka/snapshot/scala/actors.html).  Once you've customized your `Slackey` actor to your liking, you plug it into your own Actor system where it runs independently.  Behind the scenes, it fetches state, connects via websockets, spawns a number of worker actors to react to incoming messages, and handles Slack state updates so you can, for example, broadcast a message to *all* channels, including those created moments ago.

## Setup

SBT:
```sbt
scalaVersion := "2.11.5"

libraryDependencies += "com.github.slackey" %% "slackey" % "0.1"
```

## Bot Examples

See [examples in the repo](https://github.com/slackey/slackey/tree/master/src/main/scala/com/github/slackey/examples).

## API Usage

```scala
/** Here's some REPL copypasta with comments **/

// You just need an API token to start playing with the Slack API.  However,
// you SlackApi is configurable with an AsyncHttpClientConfig so you can
// tweak things like SSL, timeouts, thread pools, etc.
scala> val api = SlackApi("<token>")
api: com.github.slackey.api.SlackApi = com.github.slackey.api.SlackApi@14df389d

// As implied above, requests are made asynchronously using AsyncHttpClient.
scala> api.users.list()
res0: com.ning.http.client.ListenableFuture[com.github.slackey.api.SlackResponse[com.github.slackey.codecs.responses.UsersList]] = ...

// A `SlackResponse` is simply a type alias for `Either[SlackError, T]` where
// `SlackError` is a String (subject to change in the future) and `T` is
// typically a case class representation of the JSON response.
scala> res0.get() match {
     |   case Left(error) => println(error)
     |   case Right(response) => println(response.members.map(_.name).mkString(", "))
     | }
bottest2, larry, foo, bar

// You can also pass in a callback to every API call to stay async.
scala> api.users.list(new SlackResponseHandler[UsersList] {
     |   override def onSuccess(result: UsersList) {
     |     println(result.members.map(_.profile.first_name.getOrElse("<unknown>")).mkString(", "))
     |   }
     | })
res8: com.ning.http.client.ListenableFuture[com.github.slackey.api.SlackResponse[com.github.slackey.codecs.responses.UsersList]] = ...
<unknown>, Larry, <unknown>, <unknown>
```

#### Supported methods

The following is a list of supported Web API methods.  You can still make calls to unsupported methods by using the slightly lower level [`request` method](https://github.com/slackey/slackey/blob/624284f755dccede096fddd4e0fadfe37ee34da0/src/main/scala/com/github/slackey/api/SlackApi.scala#L175) or, better yet, submit a pull request 😅.

* auth.test
* channels.archive
* channels.create
* channels.history
* channels.info
* channels.invite
* channels.join
* channels.kick
* channels.leave
* channels.list
* channels.mark
* channels.rename
* channels.setPurpose
* channels.setTopic
* channels.unarchive
* chat.delete
* chat.postMessage
* chat.update
* emoji.list
* groups.archive
* groups.close
* groups.create
* groups.createChild
* groups.history
* groups.invite
* groups.kick
* groups.leave
* groups.list
* groups.mark
* groups.open
* groups.rename
* groups.setPurpose
* groups.setTopic
* groups.unarchive
* im.close
* im.history
* im.list
* im.mark
* im.open
* rtm.start
* users.getPresence
* users.info
* users.list
* users.setActive
* users.setPresence
