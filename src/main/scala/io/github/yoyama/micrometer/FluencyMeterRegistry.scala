package io.github.yoyama.micrometer

import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit

import io.micrometer.core.instrument.{Clock, Counter, DistributionSummary, FunctionCounter, FunctionTimer, Gauge, LongTaskTimer, Meter, Tag, TimeGauge, Timer}
import io.micrometer.core.instrument.step.{StepMeterRegistry}
import io.micrometer.core.instrument.util.{HierarchicalNameMapper, MeterPartition, NamedThreadFactory}
import org.komamitsu.fluency.fluentd.FluencyBuilderForFluentd
import org.komamitsu.fluency.{EventTime, Fluency}
import java.util.concurrent.ThreadFactory

object FluencyMeterRegistry {
  val emptyTag = Seq.empty[Tag].asJava

  def defaultThreadFactory = new NamedThreadFactory("fluency-metrics-publisher")

  def createFluency(fconfig:FluencyRegistryConfigTrait):Fluency = {
    val builder = new FluencyBuilderForFluentd()
    builder.build()
  }

  def apply(fconfig:FluencyRegistryConfigTrait, nameMapper:HierarchicalNameMapper, fclock:Clock):FluencyMeterRegistry = {
    new FluencyMeterRegistry(fconfig, nameMapper, fclock, createFluency(fconfig), defaultThreadFactory)
  }

  def apply(fconfig:FluencyRegistryConfigTrait, nameMapper:HierarchicalNameMapper, fclock:Clock, fluency:Fluency):FluencyMeterRegistry = {
    new FluencyMeterRegistry(fconfig, nameMapper, fclock, fluency, defaultThreadFactory)
  }
}

class FluencyMeterRegistry(val fconfig:FluencyRegistryConfigTrait, val nameMapper:HierarchicalNameMapper, val fclock:Clock,
                           val fluency:Fluency, val threadFactory:ThreadFactory)
                                                      extends StepMeterRegistry(fconfig, fclock) {

  start(threadFactory)


  override def start(threadFactory: ThreadFactory): Unit = super.start(threadFactory)

  override def close(): Unit = super.close()

  override def getBaseTimeUnit = TimeUnit.MILLISECONDS

  override def publish(): Unit = {
    val x: Seq[Meter] = MeterPartition.partition(this, 1).asScala
                          .toSeq.flatMap(_.asScala)
    x.foreach(publish)
  }

  case class EmitData(timestamp:Long, hash:java.util.HashMap[String, AnyRef])

  protected def publish(m:Meter):Unit = {
    val w:Option[EmitData] = m match {
      case tg: TimeGauge => writeTimeGauge(tg)
      case lt: LongTaskTimer => writeLongTaskTimer(lt)
      case ft: FunctionTimer => writeFunctionTimer(ft)
      case fc: FunctionCounter => writeFunctionCounter(fc)
      case c: Counter => writeCounter(c)
      case g: Gauge => writeGauge(g)
      case s: DistributionSummary => writeSummary(s)
      case t: Timer => writeTimer(t)
      case m: Meter => writeMeter(m)
      case _ => println("Unknown"); Option.empty
    }
    w.map( d => emit(fluency, d.timestamp, d.hash, fconfig.tag()) )
  }

  protected def writeCounter(counter: Counter): Option[EmitData] = {
    counter.count match {
      case v if (isFinite(v)) => writeCounter(counter, counter.count())
      case _ => Option.empty
    }
  }

  protected def writeCounter(counter: Meter, value: Double): Option[EmitData] = {
    val (timestamp, h) = prepareWrite(counter)
    h.put("count", toJavaDouble(value))
    Option(EmitData(timestamp, h))
  }

  protected def writeFunctionCounter(counter: FunctionCounter): Option[EmitData] = writeCounter(counter, counter.count())

  protected def writeTimer(timer: Timer): Option[EmitData] = {
    val (timestamp, h) = prepareWrite(timer)
    h.put("count", toJavaLong(timer.count()))
    h.put("sum", toJavaDouble(timer.totalTime(getBaseTimeUnit)))
    h.put("mean", toJavaDouble(timer.mean(getBaseTimeUnit)))
    h.put("max", toJavaDouble(timer.max(getBaseTimeUnit)))
    Option(EmitData(timestamp, h))
  }

  protected def writeSummary(summary: DistributionSummary): Option[EmitData] = {
    summary.takeSnapshot()
    val (timestamp, h) = prepareWrite(summary)
    h.put("count", toJavaLong(summary.count()))
    h.put("sum", toJavaDouble(summary.totalAmount()))
    h.put("mean", toJavaDouble(summary.mean()))
    h.put("max", toJavaDouble(summary.max()))
    Option(EmitData(timestamp, h))
  }

  protected def writeGauge(gauge: Gauge): Option[EmitData] = {
    gauge.value() match {
      case v:Double if(isFinite(v)) => writeGauge(gauge, v)
      case _ => Option.empty
    }
  }

  protected def writeGauge(gauge: Gauge, v:Double): Option[EmitData] = {
    val (timestamp, h) = prepareWrite(gauge)
    h.put("value", toJavaDouble(v))
    Option(EmitData(timestamp, h))
  }

  protected def writeTimeGauge(gauge: TimeGauge): Option[EmitData] = {
    gauge.value(getBaseTimeUnit) match {
      case v:Double if(isFinite(v)) => writeTimeGauge(gauge, v)
      case _ => Option.empty
    }
  }

  protected def writeTimeGauge(gauge: TimeGauge, v:Double): Option[EmitData] = {
    val (timestamp, h) = prepareWrite(gauge)
    h.put("value", toJavaDouble(v))
    Option(EmitData(timestamp, h))
  }

  protected def writeFunctionTimer(timer: FunctionTimer):Option[EmitData] = {
    val (timestamp, h) = prepareWrite(timer)
    h.put("count", toJavaDouble(timer.count()))
    h.put("sum", toJavaDouble(timer.totalTime(getBaseTimeUnit)))
    h.put("mean", toJavaDouble(timer.mean(getBaseTimeUnit)))
    Option(EmitData(timestamp, h))
  }

  protected def writeLongTaskTimer(timer: LongTaskTimer):Option[EmitData] = {
    val (timestamp, h) = prepareWrite(timer)
    h.put("activeTasks", toJavaLong(timer.activeTasks()))
    h.put("duration", toJavaDouble(timer.duration(getBaseTimeUnit)))
    Option(EmitData(timestamp, h))
  }

  protected def writeMeter(meter: Meter): Option[EmitData] = {
    val (timestamp, h) = prepareWrite(meter)
    meter.measure().asScala.foreach{ m =>
      h.put(m.getStatistic.getTagValueRepresentation, toJavaDouble(m.getValue))
    }
    Option(EmitData(timestamp, h))
  }

  protected def prepareWrite(m:Meter): (Long, java.util.HashMap[String,AnyRef]) = {
    val timestamp = config().clock().wallTime
    val name = getConventionName(m.getId)
    val h = new java.util.HashMap[String, Object]()
    h.put("name", name)
    h.put("type", m.getId.getType.toString.toLowerCase)
    (timestamp, h)
  }

  protected def emit(f:Fluency, t:Long, v:java.util.Map[String,AnyRef], tag:String): Unit = {
    f.emit(tag, EventTime.fromEpochMilli(t), v)
  }

  protected def isFinite(v:Double):Boolean = java.lang.Double.isFinite(v)

  protected def toJavaLong(v:Long) = new java.lang.Long(v)

  protected def toJavaDouble(v:Double) = new java.lang.Double(v)

}
