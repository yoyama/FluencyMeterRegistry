package org.yoyama.micrometer

import scala.collection.JavaConverters._
import java.time.Duration
import java.util
import java.util.{ArrayList, List}
import java.util.function.ToDoubleFunction

import io.micrometer.core.instrument.{Clock, Tag, Timer}
import io.micrometer.core.instrument.util.HierarchicalNameMapper
import org.komamitsu.fluency.{EventTime, Fluency}
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mockito.MockitoSugar

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class FluencyMetrRegistrySpec extends FlatSpec with Matchers with MockitoSugar{

  trait Fixture {
    val mockedFluency = mock[Fluency]
    val emitList = mutable.ListBuffer[Array[AnyRef]]()
    val gaugeTarget = mutable.ListBuffer[Integer]()

    when(mockedFluency.emit(anyString, any[EventTime], any[java.util.Map[String, Object]]))
        .thenAnswer(new Answer[Unit]() {
      override def answer(invocation: InvocationOnMock): Unit = {
        emitList += invocation.getArguments
      }
    })
  }

  "Counter" should "works well" in new Fixture {
    val m = createMeterRegistry(mockedFluency)
    for(i <- 1 to 30) {
      m.counter("count1").increment()
      Thread.sleep(1000)
    }
    m.close()
  }

  "Timer" should "works well" in  new Fixture {
    val m = createMeterRegistry(mockedFluency)
    for(i <- 1 to 30) {
      m.timer("timer1", m.emptyTag).record(Duration.ofMillis(i*100))
      Thread.sleep(1000)
    }
    m.close()
  }

  "Summary" should "works well" in  new Fixture {
    val m = createMeterRegistry(mockedFluency)
    for(i <- 1 to 30) {
      val v = i*11.1
      m.summary("summary1", m.emptyTag).record(v)
      Thread.sleep(1000)
    }
    m.close()
  }

  "Gauge" should "works well" in  new Fixture {
    val function: ToDoubleFunction[mutable.ListBuffer[Integer]] = new ToDoubleFunction[ListBuffer[Integer]] {
      override def applyAsDouble(value: ListBuffer[Integer]): Double = value.size
    }

    val m = createMeterRegistry(mockedFluency)
    for(i <- 1 to 30) {
      gaugeTarget += i
      m.gauge("gauge1", m.emptyTag, gaugeTarget, function)
      Thread.sleep(1000)
    }
    m.close()
  }


  def createMeterRegistry(fluency:Fluency):FluencyMeterRegistry = {
    val fconfig = new FluencyRegistryConfigTrait {
      override def tag(): String = "file.scala1"
      override def step(): Duration = Duration.ofSeconds(10)
    }
    FluencyMeterRegistry(fconfig, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM, fluency)
  }
}
