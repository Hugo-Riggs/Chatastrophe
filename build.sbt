name := "chatastrophe"

version := "1.0"

scalaVersion := "2.11.8"

//-----------------------------------------------------------------------------------------------------
// SBT Setup for GUI
resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)


// fix for modena style sheet not found


unmanagedJars in Compile += {
  val ps = new sys.SystemProperties
  val jh = ps("java.home")
  Attributed.blank(file(jh) / "jre/lib/ext/jfxrt.jar")
}

//unmanagedJars in Compile += Attributed.blank(file("/lib64/jdk1.8.0_91/jre/lib/ext/jfxrt.jar"))


//-----------------------------------------------------------------------------------------------------

val scalav = "2.11.8"

libraryDependencies ++= {
  Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.8",
  "com.typesafe.akka" %% "akka-agent" % "2.4.8",
  "com.typesafe.akka" %% "akka-camel" % "2.4.8",
  "com.typesafe.akka" %% "akka-cluster" % "2.4.8",
  "com.typesafe.akka" %% "akka-cluster-metrics" % "2.4.8",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.8",
  "com.typesafe.akka" %% "akka-cluster-tools" % "2.4.8",
  "com.typesafe.akka" %% "akka-contrib" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.8",
  "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.4.8",
  "com.typesafe.akka" %% "akka-osgi" % "2.4.8",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.8",
  "com.typesafe.akka" %% "akka-persistence-tck" % "2.4.8",
  "com.typesafe.akka" %% "akka-remote" % "2.4.8",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.8",
  "com.typesafe.akka" %% "akka-stream" % "2.4.8",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.8",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.8",
  "com.typesafe.akka" %% "akka-distributed-data-experimental" % "2.4.8",
  "com.typesafe.akka" %% "akka-typed-experimental" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-jackson-experimental" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.8",
  "com.typesafe.akka" %% "akka-http-xml-experimental" % "2.4.8",
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % "2.4.8",
  "org.scala-lang" % "scala-reflect" % scalav,
  "org.scalafx" %% "scalafx" % "8.0.92-R10",
  "org.scalafx" %% "scalafxml-core-sfx8" % "0.2.2"
  )
}
