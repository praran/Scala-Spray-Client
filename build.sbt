import AssemblyKeys._

name := "recs-api"

version := "1.0"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.4",
  "io.spray" %% "spray-can" % "1.3.1",
  "io.spray" %% "spray-routing" % "1.3.1",
  "io.spray" %% "spray-http" % "1.3.1",
  "io.spray" %% "spray-util" % "1.3.1",
  "io.spray" %% "spray-client" % "1.3.1",
  "io.spray" %% "spray-json" % "1.3.1",
  "io.spray" %% "spray-testkit" % "1.3.2" % "test",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.specs2" %% "specs2-core" % "2.4.11" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test"
)

assemblySettings

mainClass in assembly := Some("com.one.assignment.Application")
    