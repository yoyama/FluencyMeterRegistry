import Dependencies._

lazy val scala212 = "2.12.10"
lazy val scala213 = "2.13.1"
lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / scalaVersion     := "2.12.10"
ThisBuild / version          := "0.5.1"
ThisBuild / organization     := "io.github.yoyama"
ThisBuild / organizationName := "yoyama"
ThisBuild / description      := "Micrometer plugin for Fluency."
ThisBuild / licenses         := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage         := Some(url("https://github.com/yoyama/FluencyMeterRegistry"))
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")
ThisBuild / cancelable in Global := true
//ThisBuild / coverageEnabled := true
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/yoyama/FluencyMeterRegistry"),
    "scm:git@github.com:yoyama/FluencyMeterRegistry.git"
  )
)
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / isSnapshot := true
ThisBuild / publishTo := sonatypePublishTo.value
ThisBuild / publishMavenStyle := true
ThisBuild / developers := List(
  Developer(
    id    = "yoyama",
    name  = "You Yamagata",
    email = "youy.bg8@gmail.com",
    url   = url("https://github.com/yoyama")
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "fluency-meter-registory",
    libraryDependencies ++= Seq(
      "io.micrometer" % "micrometer-core" % "1.2.0",
      "org.komamitsu" % "fluency-core" % "2.3.3",
      "org.komamitsu" % "fluency-fluentd" % "2.3.3",
      "org.slf4j" % "slf4j-api" % "1.7.28",
      "org.slf4j" % "slf4j-simple" % "1.7.28",
      "junit" % "junit" % "4.12" % Test,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "test",
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "org.wvlet.airframe" %% "airspec" % "19.8.10" % "test",
      "org.mockito" % "mockito-all" % "1.10.19" % Test,
      "org.hamcrest" % "hamcrest-library" % "1.3" % Test
    ),
    Test / run / fork := true,
    testFrameworks += new TestFramework("wvlet.airspec.Framework")
  )

// Uncomment the following for publishing to Sonatype.
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for more detail.



