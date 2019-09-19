name := "SimpleForum"

version := "0.1"

scalaVersion := "2.13.0"

val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.9"
val scalaTestVersion = "3.0.8"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "org.scalamock" %% "scalamock" % "4.3.0" % Test
libraryDependencies += "org.scalatest" % "scalatest_2.13" % scalaTestVersion % "test"
