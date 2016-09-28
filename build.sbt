import NativePackagerHelper._

scalaVersion := "2.11.8"


libraryDependencies ++= {
  Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.10",
    "com.typesafe.akka" %% "akka-remote" % "2.4.10",
    "org.scala-lang" % "scala-reflect" % "2.11.8",
    "org.scalafx" %% "scalafx" % "8.0.102-R11",
    "org.scalafx" %% "scalafxml-core-sfx8" % "0.2.2"
  )
}

enablePlugins(JavaServerAppPackaging)

lazy val commonSettings = Seq( 
    name := "chatastrophe",
    version := "0.0.1",
    connectInput in run := true
  )


lazy val root = (project in file(".")).
  settings(commonSettings: _*)


// SBT Setup for non-gui 
lazy val client = (project in file("client")).
  settings(commonSettings: _*).
  settings(
    mainClass in (Compile) := Some("remoting.Client")
  )


// Macros allow scalafxml to generate classes (use scene builder generated fxml files)
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

//Define the java version to use
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

//Add Javafx8 library
unmanagedJars in Compile += file( "lib/ext/jfxrt.jar" )

jfxSettings


lazy val guiClient = (project in file("guiClient")).
  settings(commonSettings: _*).
  settings( 
    JFX.mainClass := Some("remoting.ClientWithGui")
  )

