package io.github.yemyatthu1990.apm;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import io.grpc.ManagedChannel;
import io.grpc.android.AndroidChannelBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class Agent {

    private static Agent instance;
    private static final String TAG = Agent.class.getName();

    public static Agent getInstance() {
        return instance;
    }


    public static void start(Application application, AgentConfiguration agentConfiguration) {
        instance = new Agent(application, agentConfiguration);
    }

    /**
     *
     * @param application application object
     * @param endpoint endpoint for reporting traces
     */
    public static void start(Application application, String endpoint) {
        AgentConfiguration configuration = new AgentConfiguration();
        configuration.setEndpoint(endpoint);
        instance = new Agent(application, configuration);
    }

    private final AgentConfiguration agentConfiguration;
    private final Application application;

    /**
     *
     * @param application application object
     * @param agentConfiguration configuration for the agent. one of endpoint or host/port must be provided
     */
    private Agent(Application application, AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
        this.application = application;
        initialize();
    }

    private void initialize() {
        Log.d(TAG, "initializing sdk");


        long sdkInitializedTime = Clock.getDefault().now();

        //Store sdk initializing events so that we can send them after the initialization is done
        List<String> listOfSdkInitializationEvents = new ArrayList<>();

        SdkInitializationInstrumentation.init(sdkInitializedTime);
        listOfSdkInitializationEvents.add("Initialize SDK instrumentation class");

        //gRPC channel for using OLTP exporter
        ManagedChannel channel = AndroidChannelBuilder.forAddress(agentConfiguration.getCollectorHost(), agentConfiguration.getCollectorPorts())
                .context(application).build();
        listOfSdkInitializationEvents.add("Initialize Method channel for gRPC connection");

        SessionManager sessionManager = new SessionManager();
        DeviceMetricsCollector deviceMetricsCollector = new DeviceMetricsCollector(application, sessionManager);
        listOfSdkInitializationEvents.add("Initialize Device Metrics collector");

        NetworkMetricCollector networkMetricCollector = new NetworkMetricCollector(application);
        listOfSdkInitializationEvents.add("Initialize Network Metrics collector");

        MemoryInfo.init(application);
        listOfSdkInitializationEvents.add("Initialize Memory info collection collector");

        RuntimeAttributesCollector runtimeAttributesCollector = new RuntimeAttributesCollector();
        listOfSdkInitializationEvents.add("Initialize Runtime attributes collector");

        SdkTracerProvider tracerProvider = getTracerProvider(application, deviceMetricsCollector, networkMetricCollector, runtimeAttributesCollector,channel);

        OpenTelemetrySdk
                .builder()
                .setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();

        listOfSdkInitializationEvents.add("Initialize OpenTelemetry Sdk");



        if (agentConfiguration.isEnableCrashMonitoring()) {
            CrashReporter.initializeCrashReporting(getTracer(), tracerProvider);
            listOfSdkInitializationEvents.add("Initialize Crash Monitoring");
        }



        AppStartInstrumentation appStartInstrumentation = AppStartInstrumentation.getInstance();
        appStartInstrumentation.start(getTracer());
        listOfSdkInitializationEvents.add("Start App Start Instrumentation");

        sessionManager.setSessionIdChangeListener(new SessionTracer(getTracer()));
        listOfSdkInitializationEvents.add("Initialize session change listener");
        List<AppState> appStates = new ArrayList<>();
        if (agentConfiguration.isEnableANRReporting()) {
            Looper mainLooper = Looper.getMainLooper();
            Thread mainThread = Looper.getMainLooper().getThread();
            Handler uiHandler = new Handler(mainLooper);

            AppState anrReporter = new ANRReporter(uiHandler, mainThread);
            appStates.add(anrReporter);
            listOfSdkInitializationEvents.add("Register activity Lifecycle Callbacks");
        }


        ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        DeprecatedAPINetworkChangeReporter deprecatedAPINetworkChangeReporter = null;

        //Use newer registerDefaultNetworkCallback for Android version later than 7
        //Fall back to good old broadcast register for old android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NetworkChangeReporter networkChangeReporter = new NetworkChangeReporter(cm, getTracer());
            cm.registerDefaultNetworkCallback(networkChangeReporter);
            appStates.add(networkChangeReporter);
        } else {
            deprecatedAPINetworkChangeReporter = new DeprecatedAPINetworkChangeReporter(cm, getTracer());
        }
        listOfSdkInitializationEvents.add("Register network change reporter");


        Application.ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifeCycleInstrumentation(appStates,deprecatedAPINetworkChangeReporter, appStartInstrumentation);

        application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
        listOfSdkInitializationEvents.add("Register activity Lifecycle Callbacks");

        SdkInitializationInstrumentation sdkInitializationInstrumentation = SdkInitializationInstrumentation.getInstance(appStartInstrumentation.getSpan());
        sdkInitializationInstrumentation.start(getTracer());

        for (String event: listOfSdkInitializationEvents) {
            if (sdkInitializationInstrumentation.getSpan() != null) {
                sdkInitializationInstrumentation.getSpan().addEvent(event);
            }
        }

        sdkInitializationInstrumentation.end();

    }

    private SdkTracerProvider getTracerProvider(Context context, DeviceMetricsCollector deviceMetricsCollector,
            NetworkMetricCollector networkMetricCollector, RuntimeAttributesCollector runtimeAttributesCollector, ManagedChannel channel) {

        SdkTracerProviderBuilder builder =
                SdkTracerProvider.builder()
                        .addSpanProcessor(getBatchSpanProcessor())
                        .addSpanProcessor(new AttributesSpanProcessor(deviceMetricsCollector, networkMetricCollector, runtimeAttributesCollector));

        if (BuildConfig.DEBUG) {
            builder.addSpanProcessor(getStdoutBatchSpanProcessor());
        }
        builder.setResource(AgentResource.get(context, deviceMetricsCollector));
        return builder.build();
    }

    private BatchSpanProcessor getStdoutBatchSpanProcessor() {
        return BatchSpanProcessor
                .builder(new StdoutExporter())
                .build();
    }

    private SpanExporter getZipkinSpanExporter() {
        ZipkinSpanExporter.baseLogger.setLevel(Level.ALL);
        return ZipkinSpanExporter
                .builder()
                .setEndpoint(agentConfiguration.getEndpoint())
                .build();
    }

//    private SpanExporter getOltpSpanExporter(ManagedChannel channel) {
//        return OtlpGrpcSpanExporter.newBuilder()
//                .setChannel(channel)
//                .build();
//    }

    private BatchSpanProcessor getBatchSpanProcessor() {
        //Either use OltpGrpcSpanExporter or ZipkinSpanExporter depend on usage
        return BatchSpanProcessor
                .builder( getZipkinSpanExporter())
                .build();
    }


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

    /**
     *
     * @param transactionName the name for transaction
     * @param attributes the extra attributes for transaction
     * @return @Tracing object which can be used to end the tracing
     */
    public Tracing startTracing(String transactionName, @Nullable Map<String, String> attributes) {
        SpanBuilder builder = getTracer().spanBuilder(transactionName);
        if (attributes != null) {
            attributes.forEach(builder::setAttribute);
        }
        Span span = builder.startSpan();
        return Tracing.fromSpan(span);
    }

    static Tracer getTracer() {
        return GlobalOpenTelemetry.getTracer(BuildConfig.LIBRARY_NAME, BuildConfig.VERSION_NAME);
    }
}