// The simplest possible sbt build file is just one line:

scalaVersion := "2.12.6"

name := "hello-world"

organization := "ch.epfl.scala"

version := "1.0"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.4.20"

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"