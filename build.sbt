ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11" // "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "formfile"
  )

// Akka
val AkkaVersion = "2.8.1"
val AkkaHttpVersion = "10.5.2"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
)


libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.7" % Test pomOnly()
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "2.0.7"
