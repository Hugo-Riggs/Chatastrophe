scalaVersion := "2.12.1"

lazy val commonSettings = Seq(
  libraryDependencies ++= {
    Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.17",
      "com.typesafe.akka" %% "akka-remote" % "2.4.17",
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    )
  }
)


lazy val serverPackage = project
  .in(file("."))
  .enablePlugins(JavaServerAppPackaging)
  .settings(commonSettings: _*)
  .settings(
    name := "chatastrophe-server",
    version := "0.0.2",
    test in assembly := {},
    assemblyJarName in assembly := "daemon.jar"
  )

/// Comment out to compile daemon, uncomment to compile client
//lazy val clientPackage = project
//  .in(file("."))
//  .enablePlugins(JavaServerAppPackaging)
//  .settings(commonSettings: _*)
//  .settings(
//    name := "chatastrophe-client",
//    version := "0.0.1",
//    test in assembly := {},
//    mainClass in assembly := Some("Chatastrophe.Actors.client.simpleInterface.SimpleInterface")
//  )
