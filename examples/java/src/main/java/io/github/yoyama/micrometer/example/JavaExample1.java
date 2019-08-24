package io.github.yoyama.micrometer.example;

import io.github.yoyama.micrometer.FluencyMeterRegistry;
import io.github.yoyama.micrometer.FluencyRegistryConfig;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import org.komamitsu.fluency.Fluency;
import org.komamitsu.fluency.fluentd.FluencyBuilderForFluentd;

import java.time.Duration;

public class JavaExample1 {
    public static void main(String[] args) throws InterruptedException
    {
        Fluency fluency = new FluencyBuilderForFluentd().build();
        FluencyRegistryConfig fconfig = new FluencyRegistryConfig("example.java", "test", Duration.ofSeconds(10));
        FluencyMeterRegistry meter = FluencyMeterRegistry.apply(fconfig, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM, fluency);
        meter.counter("count1").increment();
        meter.summary("summary1").record(999.9);
        Thread.sleep(30000);
    }
}
