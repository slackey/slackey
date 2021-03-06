package com.github.slackey.examples

import scala.io.StdIn

import akka.actor.ActorSystem
import com.ning.http.client.AsyncHttpClientConfig

import com.github.slackey.api.SlackApi
import com.github.slackey.bot.Slackey

/**
 * An example app with the example listeners installed.
 *
 * Call `main` method with a Slack API token to run.
 */
object Demo {

  private val webConfig = new AsyncHttpClientConfig.Builder()
    .setConnectTimeout(10000)
    .setRequestTimeout(10000)
    .setWebSocketTimeout(60000)
    .build()

  def main(args: Array[String]): Unit = {
    val token = args(0)
    val props = Slackey(token)
      .withHttpConfig(webConfig)
      .addListener(Echoer)
      .addListener(Announcer)
      .build
    val system = ActorSystem("demo")
    system.actorOf(props, "slackey")
    println("Enter to quit.")
    StdIn.readLine()
    system.shutdown()
  }

}
