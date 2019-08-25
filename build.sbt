import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "io.github.yoyama"
ThisBuild / organizationName := "yoyama"
ThisBuild / description      := "Micrometer plugin for Fluency."
ThisBuild / licenses         := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage         := Some(url("https://github.com/yoyama/FluencyMeterRegistry"))

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
      "com.novocode" % "junit-interface" % "0.11" % "test",
      scalaTest % Test,
      "org.mockito" % "mockito-all" % "1.10.19" % Test,
      "org.hamcrest" % "hamcrest-library" % "1.3" % Test
    ),
    Test / run / fork := true
  )

// Uncomment the following for publishing to Sonatype.
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for more detail.

ThisBuild / scmInfo := Some(
   ScmInfo(
     url("https://github.com/yoyama/FluencyMeterRegistry"),
     "scm:git@github.com:yoyama/FluencyMeterRegistry.git"
   )
)

ThisBuild / developers := List(
   Developer(
     id    = "yoyama",
     name  = "You Yamagata",
     email = "youy.bg8@gmail.com",
     url   = url("https://github.com/yoyama")
   )
)

ThisBuild / publishTo := sonatypePublishTo.value
ThisBuild / publishMavenStyle := true
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / updateOptions := updateOptions.value.withGigahorse(false)

