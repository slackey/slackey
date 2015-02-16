package com.nglarry.slacka

package object api {
  type SlackError = String
  type SlackResponse[T] = Either[SlackError, T]
}
