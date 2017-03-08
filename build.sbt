scalaVersion := "2.12.1"

// Macros allow scalafxml to generate classes (use scene builder generated fxml files)
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

lazy val commonSettings = Seq(
  libraryDependencies ++= {
      Seq(
        "com.typesafe.akka" %% "akka-actor" % "2.4.17",
        "com.typesafe.akka" %% "akka-remote" % "2.4.17",
        "org.scala-lang" % "scala-library" % scalaVersion.value,
        "org.scalafx" %% "scalafx" % "8.0.102-R11",
        "org.scalafx" %% "scalafxml-core-sfx8" % "0.3",
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
    version := "0.0.1",
    connectInput in run := true,
    mainClass in Compile := Some("Chatastrophe.interface.ServerCLI")
  )
