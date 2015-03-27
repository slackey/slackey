name := "slacka"

organization := "com.nglarry"

homepage := Some(url("https://github.com/larryng/slacka"))

startYear := Some(2015)

description := "An Akka actor playing a customizable Slack bot with state handling"

licenses += "MIT" -> url("https://github.com/larryng/slacka/blob/master/LICENSE")

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.5"

libraryDependencies += "com.ning" % "async-http-client" % "1.9.10"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.10"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"
