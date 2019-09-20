name := "SimpleForum"

version := "0.1"

scalaVersion := "2.13.0"

val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.9"
val akkaMockVersion = "4.3.0"
val scalaTestVersion = "3.0.8"
val h2Version = "1.4.199"
val slickVersion = "3.3.2"
val slf4jVersion = "1.7.26"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "org.scalamock" %% "scalamock" % akkaMockVersion % Test
libraryDependencies += "org.scalatest" %% "scalatest_2.13" % scalaTestVersion % Test
libraryDependencies += "com.h2database" %% "h2" % h2Version % Test
libraryDependencies += "com.typesafe.slick" %% "slick_2.13" % slickVersion
libraryDependencies += "org.slf4j" %% "slf4j-nop" % slf4jVersion
