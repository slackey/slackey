package com.github.slackey.api

trait SlackResponseHandler[T] {
  def onSuccess(result: T): Unit = {}
  def onSlackError(error: SlackError): Unit = {}
  def onThrowable(t: Throwable): Unit = {}
}
