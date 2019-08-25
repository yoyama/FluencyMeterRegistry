# fluency-meter-registry
[![CircleCI](https://circleci.com/gh/yoyama/FluencyMeterRegistry/tree/master.svg?style=svg)](https://circleci.com/gh/yoyama/FluencyMeterRegistry/tree/master)

A [Micrometer](https://micrometer.io/) plugin for [Fluency](https://github.com/komamitsu/fluency).
Send metrics of Micrometer to [fluentd](https://www.fluentd.org/).

## How to use (Scala)
[build.sbt](https://github.com/yoyama/FluencyMeterRegistry/blob/master/examples/scala/build.sbt)
```scala
ThisBuild / scalaVersion     := "2.12.8"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      "io.github.yoyama" % "fluency-meter-registory_2.12" % VERSION)
```

[example.scala](https://github.com/yoyama/FluencyMeterRegistry/blob/master/examples/scala/src/main/scala/example.scala)
```scala
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
[pom.xml](https://github.com/yoyama/FluencyMeterRegistry/blob/master/examples/java/pom.xml)
```
  <dependencies>
    <dependency>
      <groupId>io.github.yoyama</groupId>
      <artifactId>fluency-meter-registory_2.12</artifactId>
      <version>VERSION</version>
    </dependency>
  </dependencies>
```

[JavaExample1](https://github.com/yoyama/FluencyMeterRegistry/blob/master/examples/java/src/main/java/io/github/yoyama/micrometer/example/JavaExample1.java)
```java
        Fluency fluency = new FluencyBuilderForFluentd().build();
        FluencyRegistryConfig fconfig = new FluencyRegistryConfig("example.java", "test", Duration.ofSeconds(10));
        FluencyMeterRegistry meter = FluencyMeterRegistry.apply(fconfig, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM, fluency);
        meter.counter("count1").increment();
        meter.summary("summary1").record(999.9);
        Thread.sleep(30000);
```

