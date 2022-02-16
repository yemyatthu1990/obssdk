package io.github.yemyatthu1990.apm.instrumentation;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.github.yemyatthu1990.apm.InteractionInstrumentor;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;

public class SdkInitializationInstrumentation implements InteractionInstrumentor {
    private static long sdkInitializationTime;
    private static SdkInitializationInstrumentation instance;
    private final Span appStartSpan;
    private Span sdkInitializationSpan;
    private SdkInitializationInstrumentation(Span appStartSpan) {
        this.appStartSpan = appStartSpan;
    }
    public static void init(long sdkInitializationTime) {
        SdkInitializationInstrumentation.sdkInitializationTime =sdkInitializationTime;
    }
    public static SdkInitializationInstrumentation getInstance(Span appStartSpan) {
        if (sdkInitializationTime == 0){sdkInitializationTime = Clock.getDefault().now();}

        if (instance == null) {
            instance = new SdkInitializationInstrumentation(appStartSpan);
        }
        return instance;
    }
    @Override
    public void start(Tracer tracer) {
        if (sdkInitializationSpan != null) return;
        sdkInitializationSpan = tracer.spanBuilder("SdkInitialized")
                .setParent(appStartSpan.storeInContext(Context.current()))
                .setStartTimestamp(Clock.getDefault().now(), TimeUnit.NANOSECONDS)
                .startSpan();
    }

    @Override
    public void end() {
        if (sdkInitializationSpan != null){
            sdkInitializationSpan.end(Clock.getDefault().now(), TimeUnit.NANOSECONDS);
            sdkInitializationSpan = null;
        }
    }

    @Override
    @Nullable
    public Span getSpan() {
        return sdkInitializationSpan;
    }
}
