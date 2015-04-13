package com.github.slackey.api

/**
 * Typed Slack response handler.
 */
trait SlackResponseHandler[T] {
  def onSuccess(result: T): Unit = {}
  def onSlackError(error: SlackError): Unit = {}
  def onThrowable(t: Throwable): Unit = {}
}
