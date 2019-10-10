package io.github.yoyama.micrometer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.core.instrument.logging.LoggingRegistryConfig;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import org.junit.Before;
import org.junit.Test;
import org.komamitsu.fluency.Fluency;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.MockitoAnnotations.initMocks;

public class FluencyMeterRegistryTest
{
    List<Integer> gaugeTarget = new ArrayList<>();

    @Mock
    Fluency mockedFluency;

    List<Object[]> emitList = new ArrayList<>();
    FluencyMeterRegistry m;
    @Before
    public void setup() throws IOException
    {
        initMocks(this);
        m = createMeterRegistry();
        emitList.clear();
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) {
                emitList.add(invocation.getArguments());
                return null;
            }
        }).when(mockedFluency).emit(any(String.class), any(Long.class), any(Map.class));
        
    }

    @Test
    public void verifyCounter() throws InterruptedException
    {
        for (int i = 0; i < 10; i++) {
            m.counter("counterA").increment();
            Thread.sleep(1000);
        }
        assertThat("emit size > 0", emitList.size(), greaterThan(0));
        m.close();
    }

    @Test
    public void verifySummary() throws InterruptedException
    {
        for (int i = 0; i < 10; i++) {
            m.summary("summaryA").record(i*15.5);
            Thread.sleep(1000);
        }
        assertThat("emit size > 0", emitList.size(), greaterThan(0));
        m.close();
    }

    @Test
    public void verifyComposition() throws InterruptedException
    {

        MeterRegistry mcomp = createCompositeMeterRegistry();
        for (int i = 0; i < 10; i++) {
            mcomp.counter("counterB").increment();
            mcomp.summary("summaryB").record(i*11.1);
            gaugeTarget.add(i);
            mcomp.gauge("gauge", gaugeTarget, (x) -> x.size());
            Thread.sleep(1000);
        }
        assertThat("emit size > 0", emitList.size(), greaterThan(0));
        mcomp.close();
    }

    FluencyMeterRegistry createMeterRegistry()
    {
        FluencyRegistryConfig fconfig = FluencyRegistryConfig.apply("file.java1", "prefix1", Duration.ofSeconds(10), false);
        FluencyMeterRegistry m =  FluencyMeterRegistry.apply(fconfig, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM, mockedFluency);
        return m;
    }

    CompositeMeterRegistry createCompositeMeterRegistry()
    {
        LoggingMeterRegistry lm = new LoggingMeterRegistry(new LoggingRegistryConfig()
        {
            @Override
            public String get(String key)
            {
                return null;
            }
            @Override
            public Duration step() { return Duration.ofSeconds(5); }


        }, Clock.SYSTEM);

        return new CompositeMeterRegistry().add(createMeterRegistry()).add(lm);
    }

}
