val catsLibraries = List(
  "org.typelevel" %% "algebra" % "0.5.1",
  "org.typelevel" %% "cats" % "0.8.0",
  "org.typelevel" %% "dogs-core" % "0.3.1")

val commonsLang = List("org.apache.commons" % "commons-lang3" % "3.5")
val re2j = List("com.google.re2j" % "re2j" % "1.1")
val parboiled2 = List("org.parboiled" %% "parboiled" % "2.1.3")

lazy val commonSettings = List(
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.8.0"),
  organization := "com.alexknvl",
  version := "0.1-SNAPSHOT",
  scalaOrganization := "org.typelevel",
  scalaVersion := "2.11.8",
  scalacOptions ++= List(
    "-deprecation", "-unchecked", "-feature",
    "-encoding", "UTF-8",
    "-language:existentials", "-language:higherKinds",
    "-Yno-adapted-args", "-Ywarn-dead-code",
    "-Ywarn-numeric-widen", "-Xfuture",
    "-Ypartial-unification", "-Yliteral-types"),
  resolvers ++= List(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")),
  libraryDependencies ++= List(
    "org.scalacheck" %% "scalacheck" % "1.13.2" % "test",
    "org.typelevel" %% "discipline" % "0.7" % "test",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "com.storm-enroute" %% "scalameter" % "0.7" % "test"
  ),
  wartremoverWarnings ++= Warts.all,
  testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
  parallelExecution in Test := false)

lazy val twokenize = (project in file("twokenize")).
  settings(name := "scala-twokenize").
  settings(commonSettings: _*).
  settings(libraryDependencies ++= catsLibraries ++ commonsLang ++ re2j ++ parboiled2)

lazy val twokenizeApp = (project in file("app")).
  settings(name := "scala-twokenize-app").
  settings(commonSettings: _*).
  settings(libraryDependencies ++= catsLibraries ++ commonsLang)

lazy val root = (project in file(".")).
  settings(name := "scala-twokenize").
  settings(commonSettings: _*).
  aggregate(twokenize, twokenizeApp)