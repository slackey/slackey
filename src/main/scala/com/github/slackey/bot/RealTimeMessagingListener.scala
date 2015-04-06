package com.github.slackey.bot

import org.json4s._

import com.github.slackey.codecs.types.{BotMessage, MeMessage, SimpleMessage}

trait RealTimeMessagingListener {
  def onConnected(state: SlackState): Seq[SendMessage] = Seq.empty
  def onDisconnected(state: SlackState): Seq[SendMessage] = Seq.empty
  def onSimpleMessage(state: SlackState, message: SimpleMessage, json: JObject): Seq[SendMessage] = Seq.empty
  def onBotMessage(state: SlackState, message: BotMessage, json: JObject): Seq[SendMessage] = Seq.empty
  def onMeMessage(state: SlackState, message: MeMessage, json: JObject): Seq[SendMessage] = Seq.empty
  def onAnyMessage(state: SlackState, json: JObject): Seq[SendMessage] = Seq.empty
}
