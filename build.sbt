name := "chatastrophe"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaHttpVersion = "1.0-RC4"

  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaHttpVersion
  )
}
