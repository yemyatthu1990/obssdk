package io.github.yemyatthu1990.apm;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.function.BiConsumer;

import io.github.yemyatthu1990.apm.collectors.CpuInfo;
import io.github.yemyatthu1990.apm.collectors.RuntimeAttributesCollector;
import io.github.yemyatthu1990.apm.instrumentations.ActivityLifeCycleInstrumentation;
import io.github.yemyatthu1990.apm.instrumentations.StdoutExporter;
import io.github.yemyatthu1990.apm.monitoring.AgentResource;
import io.github.yemyatthu1990.apm.monitoring.AppState;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

public class Agent {

    private static Agent instance;

    public static Agent getInstance() {
        return instance;
    }
    public static void start(Application application, AgentConfiguration agentConfiguration) {
        instance = new Agent(application, agentConfiguration);
        instance.initialize(application);
    }

    public static void start(Application application) {
        instance = new Agent(application, new AgentConfiguration());
        instance.initialize(application);
    }

    private AgentConfiguration agentConfiguration;

    private Agent(Application context, AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
        System.out.println("Span registering lifecycle callbacks?");
        try {
            OpenTelemetrySdk
                    .builder()
                    .setTracerProvider(getTracerProvider(context))
                    .buildAndRegisterGlobal();
        } catch (Exception e) {}
        Application.ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifeCycleInstrumentation(new AppState() {
            @Override
            public void onAppEnterBackground() {

            }

            @Override
            public void onAppEnterForeground() {

            }
        });
        context.registerActivityLifecycleCallbacks(lifecycleCallbacks);

//        new RuntimeAttributesCollector().getRuntimeAttributes().forEach((key, value) -> {
//            System.out.println(key+" : "+ value);
//        });
//        for(AttributeKey key : AgentResource.get(context).getAttributes().asMap().keySet()) {
//            System.out.println("finding attributes");
//            System.out.println(key.getKey() + " : "+ key.getType().name());
//            System.out.println(AgentResource.get(context).getAttribute(key));
//        }
//
    }
    private void initialize(Application application) {


    }

    private SdkTracerProvider getTracerProvider(Context context) {
        return SdkTracerProvider.builder()
                .setClock(Clock.getDefault())
                .addSpanProcessor(getBatchSpanProcessor())
                .setResource(AgentResource.get(context))
                .build();
    }

    private SpanExporter getSpanExporter() {
        return new StdoutExporter(false);
    }

    private BatchSpanProcessor getBatchSpanProcessor() {
        return BatchSpanProcessor.builder(getSpanExporter())
                .build();
    }
}