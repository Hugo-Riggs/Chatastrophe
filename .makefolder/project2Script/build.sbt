scalaVersion := "2.11.8"

// Macros allow scalafxml to generate classes (use scene builder generated fxml files)
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)


lazy val commonSettings = Seq(
  libraryDependencies ++= {
      Seq(
        "com.typesafe.akka" %% "akka-actor" % "2.4.10",
        "com.typesafe.akka" %% "akka-remote" % "2.4.10",
        "org.scala-lang" % "scala-reflect" % "2.11.8",
        "org.scalafx" %% "scalafx" % "8.0.102-R11",
        "org.scalafx" %% "scalafxml-core-sfx8" % "0.2.2",
        "org.scalatest" %% "scalatest" % "3.0.0" % "test"
      )
  }
)


lazy val clientCliPackage = project
  .in(file("."))
  .enablePlugins(JavaServerAppPackaging)
  .settings(commonSettings: _*)
  .settings(
    name := "chatastrophe-client",
    version := "0.0.1",
    connectInput in run := true,
    mainClass in Compile := Some("interface.ClientWithCLI")
  )
