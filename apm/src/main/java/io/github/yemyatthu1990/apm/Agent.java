package io.github.yemyatthu1990.apm;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.time.Duration;
import java.util.logging.Level;

import io.github.yemyatthu1990.apm.instrumentations.ActivityLifeCycleInstrumentation;
import io.github.yemyatthu1990.apm.instrumentations.AppStartInstrumentation;
import io.github.yemyatthu1990.apm.instrumentations.CPUSampler;
import io.github.yemyatthu1990.apm.instrumentations.StdoutMetricsExporter;
import io.github.yemyatthu1990.apm.monitoring.AgentResource;
import io.github.yemyatthu1990.apm.monitoring.AppState;
import io.grpc.ManagedChannel;
import io.grpc.android.AndroidChannelBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class Agent {

    private static Agent instance;
    private AppStartInstrumentation appStartInstrumentation;
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
        ManagedChannel channel = AndroidChannelBuilder.forAddress(agentConfiguration.getCollectorHost(), agentConfiguration.getCollectorPorts())
                .context(context).build();

        try {
            OpenTelemetrySdk
                    .builder()
                    .setTracerProvider(getTracerProvider(context, channel))
                    .setMeterProvider(getMetaProvider(context, channel))
                    .buildAndRegisterGlobal();
        } catch (Exception e) {}

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
        System.out.println("possible app startup");
        appStartInstrumentation = new AppStartInstrumentation();
        appStartInstrumentation.start(GlobalOpenTelemetry.getTracer("AppStart", BuildConfig.VERSION_NAME));
        Application.ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifeCycleInstrumentation(new AppState() {
            @Override
            public void onAppEnterBackground() {}

            @Override
            public void onAppEnterForeground() {}
        }, appStartInstrumentation);
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
        CPUSampler.startSampling();
    }

    private SdkTracerProvider getTracerProvider(Context context, ManagedChannel channel) {
        return SdkTracerProvider.builder()
                .addSpanProcessor(getBatchSpanProcessor(context , channel))
                .setResource(AgentResource.get(context))
                .build();
    }

    private SpanExporter getSpanExporter() {
        ZipkinSpanExporter.baseLogger.setLevel(Level.ALL);
        return ZipkinSpanExporter
                .builder()
                .setEndpoint("http://10.228.213.101:9411/api/v2/spans")
                .build();
    }

    private BatchSpanProcessor getBatchSpanProcessor(Context context, ManagedChannel channel) {
        return BatchSpanProcessor
                .builder( getSpanExporter())
                .build();
    }

    private SdkMeterProvider getMetaProvider(Context context, ManagedChannel channel) {
            return SdkMeterProvider.builder()
                    .registerMetricReader(
                            PeriodicMetricReader.builder(new StdoutMetricsExporter())
                                    .setInterval(Duration.ofSeconds(5))
                                    .newMetricReaderFactory())
                    .setResource(AgentResource.get(context))
                    .build();

//        }  else {
//            return SdkMeterProvider.builder()
//                    .registerMetricReader(
//                            PeriodicMetricReader.builder(OtlpGrpcMetricExporter.newBuilder()
//                                            .setChannel(channel)
//                                            .build())
//                                    .setInterval(Duration.ofSeconds(1))
//                                    .newMetricReaderFactory())
//                    .setResource(AgentResource.get(context))
//                    .build();
//        }
    }
}