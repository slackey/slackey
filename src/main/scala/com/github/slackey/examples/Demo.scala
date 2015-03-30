package com.github.slackey.examples

import scala.io.StdIn

import akka.actor.ActorSystem
import com.ning.http.client.AsyncHttpClientConfig

import com.github.slackey.bot.Slackey

object Demo {

  private val webConfig = new AsyncHttpClientConfig.Builder()
    .setConnectTimeout(10000)
    .setRequestTimeout(10000)
    .setAcceptAnyCertificate(true)
    .build()

  private val websocketConfig = new AsyncHttpClientConfig.Builder()
    .setConnectTimeout(10000)
    .setRequestTimeout(10000)
    .setAcceptAnyCertificate(true)
    .setWebSocketTimeout(60000)
    .build()

  def main(args: Array[String]): Unit = {
    val token = args(0)
    val props = Slackey(token)
      .withWebConfig(webConfig)
      .withWebSocketConfig(websocketConfig)
      .addListener(EchoListener)
      .build
    val system = ActorSystem("demo")
    system.actorOf(props, "slackey")
    println("Enter to quit.")
    StdIn.readLine()
    system.shutdown()
  }

}
