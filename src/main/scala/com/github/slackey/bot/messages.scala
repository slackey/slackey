package com.github.slackey.bot

import org.json4s.JObject

protected case class Connected(state: SlackState)
protected case class Disconnected(state: SlackState)
protected case class ReceiveMessage(state: SlackState, json: JObject)

/**
 * Actor message to send a Slack message through websockets.
 */
case class SendMessage(id: Long, channel: String, text: String)

