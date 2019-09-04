package io.github.yoyama.micrometer

import java.time.Duration
import java.util.function.ToDoubleFunction

import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.core.instrument.logging.{LoggingMeterRegistry, LoggingRegistryConfig}
import io.micrometer.core.instrument.{Clock, Counter}
import io.micrometer.core.instrument.util.HierarchicalNameMapper
import org.komamitsu.fluency.{EventTime, Fluency}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import wvlet.airspec._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class FluencyMeterRegistryAirSpec extends AirSpec {

  def counterTest(): Unit = new Fixture {
    val m = createMeterRegistry(mockedFluency)
    for(i <- 1 to 10) {
      m.counter("count1").increment()
      Thread.sleep(1000)
    }
    assert(emitList.size > 0)
    m.close()
  }

  def timerTest(): Unit = new Fixture {
    val m = createMeterRegistry(mockedFluency)
    for(i <- 1 to 10) {
      m.timer("timer1", FluencyMeterRegistry.emptyTag).record(Duration.ofMillis(i*100))
      Thread.sleep(1000)
    }
    assert(emitList.size > 0)
    m.close()
  }
  
  def summaryTest(): Unit = new Fixture {
    val m = createMeterRegistry(mockedFluency)
    for(i <- 1 to 10) {
      val v = i*11.1
      m.summary("summary1", FluencyMeterRegistry.emptyTag).record(v)
      Thread.sleep(1000)
    }
    assert(emitList.size > 0)
    m.close()
  }

  def gaugeTest(): Unit = new Fixture {
    val function: ToDoubleFunction[mutable.ListBuffer[Integer]] = new ToDoubleFunction[ListBuffer[Integer]] {
      override def applyAsDouble(value: ListBuffer[Integer]): Double = value.size
    }

    val m = createMeterRegistry(mockedFluency)
    for(i <- 1 to 10) {
      gaugeTarget += i
      m.gauge("gauge1", FluencyMeterRegistry.emptyTag, gaugeTarget, function)
      Thread.sleep(1000)
    }
    assert(emitList.size > 0)
    m.close()
  }

  def compositedMeterRegistryTest() : Unit = new FixtureComposition {
    val m = createCompositedMeterRegistry()
    for(i <- 1 to 10) {
      val v = i*11.1
      m.summary("summary1", FluencyMeterRegistry.emptyTag).record(v)
      Thread.sleep(1000)
    }
    assert(emitList.size > 0)
    m.close()
  }

  def metricsTagTest(): Unit = new Fixture {
    val m = createMeterRegistry(mockedFluency)
    val cnt = Counter.builder("count1").tags("col1", "val1", "col2", "val2").register(m)
    for(i <- 1 to 10) {
      cnt.increment()
      Thread.sleep(1000)
    }
    assert(emitList.size > 0)
    val taggedCount = emitList.map(_.toList.last).foldLeft(0){ (acc, v) =>
      val str = v.toString
      println(str) // "{tag_col2=val2, tag_col1=val1, name=count1, count=5.0, type=counter}"
      if(str.matches(".*tag_col1=val1.*") && str.matches(".*tag_col2=val2.*"))
        acc+1
      else
        acc
    }
    assert(taggedCount > 0)
    m.close()
  }

  def disableMetricsTagTst(): Unit = new Fixture {
    val m = createMeterRegistryWithDisableTag(mockedFluency)
    val cnt = Counter.builder("count1").tags("col1", "val1", "col2", "val2").register(m)
    for(i <- 1 to 10) {
      cnt.increment()
      Thread.sleep(1000)
    }
    assert(emitList.size > 0)
    val taggedCount = emitList.map(_.toList.last).foldLeft(0){ (acc, v) =>
      val str = v.toString
      println(str) // "{tag_col2=val2, tag_col1=val1, name=count1, count=5.0, type=counter}"
      if(str.matches(".*tag_col1=val1.*") || str.matches(".*tag_col2=val2.*"))
        acc+1
      else
        acc
    }
    assert(taggedCount == 0) // metrics tag is disabled. should not be found tagged record
    m.close()
  }

  trait FixtureComposition extends Fixture {
    def createCompositedMeterRegistry(): CompositeMeterRegistry = {
      val lm = new LoggingMeterRegistry(new LoggingRegistryConfig() {
        override def get(key: String): String = null

        override def step: Duration = Duration.ofSeconds(10)
      }, Clock.SYSTEM)
      new CompositeMeterRegistry().add(createMeterRegistry(mockedFluency)).add(lm)
    }
  }

  trait Fixture {
    val emitList = mutable.ListBuffer[Array[AnyRef]]()
    val gaugeTarget = mutable.ListBuffer[Integer]()
    val mockedFluency = mock[Fluency](classOf[Fluency])

    when(mockedFluency.emit(anyString, any[EventTime], any[java.util.Map[String, Object]]))
      .thenAnswer(new Answer[Unit]() {
        override def answer(invocation: InvocationOnMock): Unit = {
          emitList += invocation.getArguments
        }
      })

    def createMeterRegistry(fluency:Fluency):FluencyMeterRegistry = {
      val fconfig = new FluencyRegistryConfig {
        override def tag(): String = "file.scala1"
        override def step(): Duration = Duration.ofSeconds(5)
      }
      FluencyMeterRegistry(fconfig, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM, fluency)
    }

    def createMeterRegistryWithDisableTag(fluency:Fluency):FluencyMeterRegistry = {
      val fconfig = new FluencyRegistryConfig {
        override def tag(): String = "file.scala1"
        override def step(): Duration = Duration.ofSeconds(5)
        override def disableMetricsTag(): Boolean = true
      }
      FluencyMeterRegistry(fconfig, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM, fluency)
    }
  }
}
