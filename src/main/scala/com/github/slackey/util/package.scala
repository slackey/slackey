package com.github.slackey

package object util {
  def randomReplyId = (Math.random() * Long.MaxValue).toLong
}
