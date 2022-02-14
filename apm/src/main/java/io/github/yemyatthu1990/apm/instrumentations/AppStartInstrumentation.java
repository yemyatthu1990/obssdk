package io.github.yemyatthu1990.apm.instrumentations;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.Clock;

/**
 * Class to instrument the app startup span
 */
public class AppStartInstrumentation {
    private Span appStartupSpan;
    public Span start(Tracer tracer) {
        if (appStartupSpan != null) return appStartupSpan;
        appStartupSpan = tracer.spanBuilder("AppStart")
                .setStartTimestamp(Clock.getDefault().now(), TimeUnit.NANOSECONDS)
                .startSpan();
        return appStartupSpan;
    }

    public void end() {
        if (appStartupSpan != null){
            appStartupSpan.end(Clock.getDefault().now(), TimeUnit.NANOSECONDS);
            appStartupSpan = null;
        }
    }

    @Nullable
    public Span getStartupSpan() {
        return appStartupSpan;
    }
}
