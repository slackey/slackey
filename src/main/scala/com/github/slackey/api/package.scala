package com.github.slackey

package object api {
  type SlackError = String
  type SlackResponse[T] = Either[SlackError, T]
}
