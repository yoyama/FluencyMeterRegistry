# fluency-meter-registry

A [Micrometer](https://micrometer.io/) plugin for [Fluency](https://github.com/komamitsu/fluency).
Send metrics of Micrometer to [fluentd](https://www.fluentd.org/).

## How to use (Scala)
build.sbt
```scala
ThisBuild / scalaVersion     := "2.12.8"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/\
repositories/snapshots"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "io.github.yoyama" % "fluency-meter-registory_2.12" % "0.1\
.0-SNAPSHOT",
      "io.micrometer" % "micrometer-core" % "1.2.0",
      "org.komamitsu" % "fluency-core" % "2.3.3",
      "org.komamitsu" % "fluency-fluentd" % "2.3.3")
```

example.scala
```scala
import java.time.Duration

import io.github.yoyama.micrometer.{FluencyMeterRegistry, FluencyRegistryConfigTrait}
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.util.HierarchicalNameMapper
import org.komamitsu.fluency.Fluency
import org.komamitsu.fluency.fluentd.FluencyBuilderForFluentd

object Example {
    def main(args: Array[String]): Unit ={
        val fluency:Fluency = new FluencyBuilderForFluentd().build()
        val fconfig = new FluencyRegistryConfigTrait {
            override def prefix(): String = "default"
            override def tag(): String = "example"
            override def step(): Duration = Duration.ofSeconds(10)
        }
        val m = FluencyMeterRegistry(fconfig, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM, fluency)
        m.counter("count1").increment()
        m.summary("summary1").record(999.9)
    }
}
```
## How to use (Java)
```java

```

