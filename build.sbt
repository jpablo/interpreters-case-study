// Turn this project into a Scala.js project by importing these settings
enablePlugins(ScalaJSPlugin)

name := "Interpreters Case Study"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.1"

scalaJSModuleKind := ModuleKind.CommonJSModule


//persistLauncher in Compile := true

//persistLauncher in Test := false

testFrameworks += new TestFramework("utest.runner.Framework")

val monixVersion = "2.3.0"

libraryDependencies ++= Seq(
  "com.lihaoyi" %%% "utest" % "0.4.5" % "test",
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "io.monix" %%% "monix" % monixVersion,
  "io.monix" %%% "monix-cats" % monixVersion,
  "fr.hmil" %%% "roshttp" % "2.0.1",
  "com.47deg" %%% "fetch" % "0.5.0",
  "org.typelevel" %%% "cats" % "0.9.0",
  "org.typelevel" %%% "cats-free" % "0.9.0"

)

val circeVersion = "0.7.1"

libraryDependencies ++= Seq(
  "io.circe" %%% "circe-core",
  "io.circe" %%% "circe-generic",
  "io.circe" %%% "circe-parser"
).map(_ % circeVersion)
