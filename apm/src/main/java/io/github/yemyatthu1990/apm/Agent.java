package io.github.yemyatthu1990.apm;

import android.content.Context;
import android.renderscript.Sampler;

import java.util.function.BiConsumer;

import io.github.yemyatthu1990.apm.collectors.CpuInfo;
import io.github.yemyatthu1990.apm.collectors.RuntimeAttributesCollector;
import io.github.yemyatthu1990.apm.monitoring.AgentResource;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

public class Agent {

    private static Agent instance;

    public static Agent getInstance() {
        return instance;
    }
    public static void start(Context context, AgentConfiguration agentConfiguration) {
        instance = new Agent(context, agentConfiguration);
        instance.initialize();
    }

    public static void start(Context context) {
        instance = new Agent(context, new AgentConfiguration());
        instance.initialize();

    }

    private AgentConfiguration agentConfiguration;

    private Agent(Context context, AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
        OpenTelemetrySdk
                .builder()
                .setTracerProvider(getTracerProvider(context))
                .buildAndRegisterGlobal();
        new RuntimeAttributesCollector().getRuntimeAttributes().forEach((key, value) -> {
            System.out.println(key+" : "+ value);
        });
        for(AttributeKey key : AgentResource.get(context).getAttributes().asMap().keySet()) {
            System.out.println("finding attributes");
            System.out.println(key.getKey() + " : "+ key.getType().name());
            System.out.println(AgentResource.get(context).getAttribute(key));
        }

    }
    private void initialize() {
        System.out.println("another usage: "+CpuInfo.getCpuUsageFromFreq());
    }

    private SdkTracerProvider getTracerProvider(Context context) {
        return SdkTracerProvider.builder()
                .setClock(Clock.getDefault())
                .setResource(AgentResource.get(context))
                .build();
    }
}