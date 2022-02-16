package io.github.yemyatthu1990.apm.sampler;

import io.github.yemyatthu1990.apm.collector.CpuInfo;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

public class CPUSampler {
    private CPUSampler() {

    }

    public static void startSampling() {
        GlobalOpenTelemetry.getMeterProvider().get("CPU Sampler")
                .gaugeBuilder("system.cpu.usage")
                .setDescription("CPU Usage of System")
                .setUnit("percentage")
                .buildWithCallback(observableDoubleMeasurement ->
                        observableDoubleMeasurement.record(CpuInfo.getCpuUsageFromFreq(), Attributes.of(AttributeKey.stringKey("state"), "app")));
    }

    public static void stopSampling() {

    }
}
