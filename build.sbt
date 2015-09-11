name := "slackey"

organization := "com.github.slackey"

homepage := Some(url("https://github.com/slackey/slackey"))

startYear := Some(2015)

description := "An Akka actor playing a customizable Slack bot with state handling"

version := "0.1.1"

licenses += "MIT" -> url("https://github.com/slackey/slackey/blob/master/LICENSE")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra :=
  <scm>
    <url>git@github.com:slackey/slackey.git</url>
    <connection>scm:git:git@github.com:slackey/slackey.git</connection>
  </scm>
  <developers>
    <developer>
      <id>larryng</id>
      <name>Larry Ng</name>
      <url>https://github.com/larryng</url>
    </developer>
  </developers>

scalaVersion := "2.11.5"

libraryDependencies += "com.ning" % "async-http-client" % "1.9.10"

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.2.10"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"
