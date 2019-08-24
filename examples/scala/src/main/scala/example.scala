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
        Thread.sleep(30000)
    }
}