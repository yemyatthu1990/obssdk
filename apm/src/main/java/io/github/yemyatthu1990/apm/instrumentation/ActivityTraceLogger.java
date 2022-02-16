package io.github.yemyatthu1990.apm.instrumentation;

import android.app.Activity;

import io.github.yemyatthu1990.apm.Agent;
import io.github.yemyatthu1990.apm.instrumentation.AppStartInstrumentation;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;

class ActivityTraceLogger extends TraceLogger {
    static final AttributeKey<String> activityKey = AttributeKey.stringKey("activityName");
    private final Activity activity;
    private final AppStartInstrumentation appStartInstrumentation;
    private final String activityCreationSpanKey = "activity.created";

    ActivityTraceLogger(Activity activity, AppStartInstrumentation appStartInstrumentation) {
        super(Agent.getTracer());
        this.activity = activity;
        this.appStartInstrumentation = appStartInstrumentation;
    }
    void startNormalActivityCreationSpan() {
         startSpan(activityCreationSpanKey, null);
    }

    void startAppStartActivityCreationSpan() {
        if (appStartInstrumentation != null) {
            startSpan(activityCreationSpanKey, appStartInstrumentation.getSpan());
        }
    }

    void startAppForegroundSpan() {
        String appForegroundSpanKey = "app.foreground";
        startSpan(appForegroundSpanKey, null);
    }

    void startAppBackgroundSpan() {
        String appBackgroundSpanKey = "app.background";
        startSpan(appBackgroundSpanKey, null);
    }

    void startActivityDestroyedSpan() {
        String appDestroyedKey = "app.destroyed";
        startSpan(appDestroyedKey, null);
    }

    @Override
    Span createSpan(String spanName, Span parent) {
        final SpanBuilder spanBuilder = tracer.spanBuilder(spanName);
        spanBuilder.setSpanKind(SpanKind.CLIENT);
        //set activity related attributes here
         if (parent != null) {
             spanBuilder.setParent(parent.storeInContext(Context.current()));
         }
        spanBuilder.setAttribute(activityKey, activity.getClass().getName());
        return spanBuilder.startSpan();
    }
}
