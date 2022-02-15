package io.github.yemyatthu1990.apm;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.Clock;

/**
 * Class to instrument the app startup span
 */
class AppStartInstrumentation implements InteractionInstrumentor {
    private static long appStartTime;
    private static AppStartInstrumentation instance;
    private AppStartInstrumentation() {

    }
    public static void init(long appStartTime) {
        AppStartInstrumentation.appStartTime = appStartTime;
    }
    public static AppStartInstrumentation getInstance() {

        //Failed initialization in app start automatically. Initialize manually here.
        if (appStartTime == 0){appStartTime = Clock.getDefault().now();}

        if (instance == null) {
            instance = new AppStartInstrumentation();
        }
        return instance;
    }
    private Span appStartupSpan;
    @Override
    public void start(Tracer tracer) {
        if (appStartupSpan != null) return;
        appStartupSpan = tracer.spanBuilder("AppStart")
                .setStartTimestamp(appStartTime, TimeUnit.NANOSECONDS)
                .startSpan();
    }
    @Override
    public void end() {
        if (appStartupSpan != null){
            appStartupSpan.end(Clock.getDefault().now(), TimeUnit.NANOSECONDS);
            appStartupSpan = null;
        }
    }

    @Override
    @Nullable
    public Span getSpan() {
        return appStartupSpan;
    }
}
