package com.github.slackey.bot

import org.json4s._

import com.github.slackey.codecs.types.{BotMessage, MeMessage, SimpleMessage}

/**
 * A listener that can respond to various Slack RTM events.
 *
 * By default, each method does nothing.  Overriden methods can return a
 * `Seq[SendMessage]`, each representing a message to send over websockets.
 */
trait RealTimeMessagingListener {
  def onConnected(state: SlackState): Seq[SendMessage] = Seq.empty
  def onDisconnected(state: SlackState): Seq[SendMessage] = Seq.empty
  def onSimpleMessage(state: SlackState, message: SimpleMessage, json: JObject): Seq[SendMessage] = Seq.empty
  def onBotMessage(state: SlackState, message: BotMessage, json: JObject): Seq[SendMessage] = Seq.empty
  def onMeMessage(state: SlackState, message: MeMessage, json: JObject): Seq[SendMessage] = Seq.empty
  def onAnyMessage(state: SlackState, json: JObject): Seq[SendMessage] = Seq.empty
}
