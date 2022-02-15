package io.github.yemyatthu1990.apm;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.startup.AppInitializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import io.github.yemyatthu1990.apm.collectors.CpuInfo;
import io.github.yemyatthu1990.apm.collectors.DeviceMetricsCollector;
import io.github.yemyatthu1990.apm.collectors.MemoryInfo;
import io.github.yemyatthu1990.apm.collectors.NetworkMetricCollector;
import io.github.yemyatthu1990.apm.collectors.RuntimeAttributesCollector;
import io.github.yemyatthu1990.apm.instrumentations.ActivityLifeCycleInstrumentation;
import io.github.yemyatthu1990.apm.instrumentations.AppStartInstrumentation;
import io.github.yemyatthu1990.apm.instrumentations.CPUSampler;
import io.github.yemyatthu1990.apm.instrumentations.SdkInitializationInstrumentation;
import io.github.yemyatthu1990.apm.instrumentations.StdoutMetricsExporter;
import io.github.yemyatthu1990.apm.monitoring.AgentResource;
import io.github.yemyatthu1990.apm.monitoring.AppState;
import io.grpc.ManagedChannel;
import io.grpc.android.AndroidChannelBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class Agent {

    private static Agent instance;
    private AppStartInstrumentation appStartInstrumentation;
    private SdkInitializationInstrumentation sdkInitializationInstrumentation;
    private static String TAG = Agent.class.getName();
    public static Agent getInstance() {
        return instance;
    }
    public static void start(Application application, AgentConfiguration agentConfiguration) {
        instance = new Agent(application, agentConfiguration);
    }

    public static void start(Application application) {
        instance = new Agent(application, new AgentConfiguration());
    }

    private AgentConfiguration agentConfiguration;
    private Application application;
    private Agent(Application application, AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
        this.application = application;
        initialize();

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
    private void initialize() {
        Log.d(TAG, "initializing sdk");
        long sdkInitializedTime = Clock.getDefault().now();
        List<String> listOfSdkInitializationEvents = new ArrayList<>();
        SdkInitializationInstrumentation.init(sdkInitializedTime);
        listOfSdkInitializationEvents.add("Initialize SDK instrumentation class");
        ManagedChannel channel = AndroidChannelBuilder.forAddress(agentConfiguration.getCollectorHost(), agentConfiguration.getCollectorPorts())
                .context(application).build();
        listOfSdkInitializationEvents.add("Initialize Method channel for gRPC connection");
        DeviceMetricsCollector deviceMetricsCollector = new DeviceMetricsCollector(application);
        listOfSdkInitializationEvents.add("Initialize Device Metrics collector");
        NetworkMetricCollector networkMetricCollector = new NetworkMetricCollector(application);
        listOfSdkInitializationEvents.add("Initialize Network Metrics collector");
        MemoryInfo.init(application);
        listOfSdkInitializationEvents.add("Initialize Memory info collection collector");
        RuntimeAttributesCollector runtimeAttributesCollector = new RuntimeAttributesCollector();
        listOfSdkInitializationEvents.add("Initialize Runtime attributes collector");


        try {
            OpenTelemetrySdk
                    .builder()
                    .setTracerProvider(getTracerProvider(application, deviceMetricsCollector, networkMetricCollector, runtimeAttributesCollector,channel))
                    .setMeterProvider(getMetaProvider(application, deviceMetricsCollector,channel))
                    .buildAndRegisterGlobal();
        } catch (Exception ignored) {}

        listOfSdkInitializationEvents.add("Initialize OpenTelemetry Sdk");
        appStartInstrumentation = AppStartInstrumentation.getInstance();
        appStartInstrumentation.start(GlobalOpenTelemetry.getTracer("AppStart", BuildConfig.VERSION_NAME));
        listOfSdkInitializationEvents.add("Start App Start Instrumentation");

        Application.ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifeCycleInstrumentation(new AppState() {
            @Override
            public void onAppEnterBackground() {
                //TODO disable metric reporting in background
            }

            @Override
            public void onAppEnterForeground() {
                //TODO re-enable metric reporting in foreground
            }
        }, appStartInstrumentation);
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
        listOfSdkInitializationEvents.add("Register activity Lifecycle Callbacks");
        CPUSampler.startSampling();

        listOfSdkInitializationEvents.add("Start CPU Sampling");
        sdkInitializationInstrumentation = SdkInitializationInstrumentation.getInstance(appStartInstrumentation.getSpan());
        sdkInitializationInstrumentation.start(GlobalOpenTelemetry.getTracer("SDKStart", BuildConfig.VERSION_NAME));
        for (String event: listOfSdkInitializationEvents) {
            if (sdkInitializationInstrumentation.getSpan() != null) {
                sdkInitializationInstrumentation.getSpan().addEvent(event);
            }
        }
        sdkInitializationInstrumentation.end();

    }

    private SdkTracerProvider getTracerProvider(Context context, DeviceMetricsCollector deviceMetricsCollector,
            NetworkMetricCollector networkMetricCollector, RuntimeAttributesCollector runtimeAttributesCollector, ManagedChannel channel) {
        return SdkTracerProvider.builder()
                .addSpanProcessor(getBatchSpanProcessor(context , channel))
                .addSpanProcessor(new AttributesSpanProcessor(deviceMetricsCollector, networkMetricCollector, runtimeAttributesCollector))
                .setResource(AgentResource.get(context, deviceMetricsCollector))
                .build();
    }

    private SpanExporter getSpanExporter() {
        ZipkinSpanExporter.baseLogger.setLevel(Level.ALL);
        return ZipkinSpanExporter
                .builder()
                .setEndpoint("http://10.10.10.67:9411/api/v2/spans")
                .build();
    }

    private BatchSpanProcessor getBatchSpanProcessor(Context context, ManagedChannel channel) {
        return BatchSpanProcessor
                .builder( getSpanExporter())
                .build();
    }

//    private BatchSpanProcessor getAttributesSpanProcess(Context context< ManagedChannel) {
//        return
//    }


    private SdkMeterProvider getMetaProvider(Context context, DeviceMetricsCollector deviceMetricsCollector,ManagedChannel channel) {
            return SdkMeterProvider.builder()
                    .registerMetricReader(
                            PeriodicMetricReader.builder(new StdoutMetricsExporter())
                                    .setInterval(Duration.ofSeconds(5))
                                    .newMetricReaderFactory())
                    .setResource(AgentResource.get(context, deviceMetricsCollector))
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