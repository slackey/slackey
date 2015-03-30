# slackey
Slackey is a library for using [Slack's Real Time Messaging API](https://api.slack.com/rtm).  Also included is an interface for the [Web API](https://api.slack.com/web), but it's mostly about making your own 11Slack lackey (read: bot).

A *reliable*, *fast*, *easy-to-use*, and *customizable* Slack bot.

Slackey is built using [Akka actors](http://doc.akka.io/docs/akka/snapshot/scala/actors.html), which is where the "reliable" and "fast" part come in.  Once you've customized your `Slackey` actor to your liking, you just plug it into your own Actor system where it runs independently.  Under the hood, it fetches state, connects via websockets, spawns a number of worker actors, and handles Slack state updates so you can, for example, broadcast a message to all channels your bot is in quickly and reliably.

### Examples

See [examples in the repo](https://github.com/slackey/slackey/tree/master/src/main/scala/com/github/slackey/examples).
