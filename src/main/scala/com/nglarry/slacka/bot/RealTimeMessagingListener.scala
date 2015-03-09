package com.nglarry.slacka.bot

import org.json4s._

import com.nglarry.slacka.codecs.types.Message

trait RealTimeMessagingListener {
  import com.nglarry.slacka.bot.BotMessages._

  def onConnected(state: SlackState): Seq[SendMessage] = Seq.empty
  def onDisconnected(state: SlackState): Seq[SendMessage] = Seq.empty
  def onSimpleMessage(state: SlackState, message: Message, json: JObject): Seq[SendMessage] = Seq.empty
  def onBotMessage(state: SlackState, message: Message, json: JObject): Seq[SendMessage] = Seq.empty
  def onMeMessage(state: SlackState, message: Message, json: JObject): Seq[SendMessage] = Seq.empty
  def onAnyMessage(state: SlackState, json: JObject): Seq[SendMessage] = Seq.empty
}
