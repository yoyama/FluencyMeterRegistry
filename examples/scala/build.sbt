ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / name := "fluency-meter-registry-example-scala",

resolvers += 
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val root = (project in file("."))
  .settings(
    name := "fluency-meter-registry-example-scala",
    libraryDependencies ++= Seq(
      "io.github.yoyama" % "fluency-meter-registory_2.12" % "0.1.0-SNAPSHOT",
    )
  )
  
