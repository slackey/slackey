package com.nglarry.slacka

package object util {
  def randomReplyId = (Math.random() * Long.MaxValue).toLong
}
