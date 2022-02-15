package io.github.yemyatthu1990.apm.log;

import android.app.Activity;

import io.github.yemyatthu1990.apm.instrumentations.AppStartInstrumentation;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

public class ActivityTraceLogger extends TraceLogger{
    static final AttributeKey<String> ACTIVITY_NAME_KEY = AttributeKey.stringKey("activityName");
    private final Activity activity;
    private AppStartInstrumentation appStartInstrumentation;
    private final String ACTIVITY_CREATION_SPAN_NAME_KEY = "Activity Created";
    public ActivityTraceLogger(Tracer tracer, Activity activity, AppStartInstrumentation appStartInstrumentation) {
        super(tracer);
        this.activity = activity;
        this.appStartInstrumentation = appStartInstrumentation;
    }
    public void startNormalActivityCreationSpan() {
         startSpan(ACTIVITY_CREATION_SPAN_NAME_KEY, null);
    }

    public void startAppstartActivityCreationSpan() {
        if (appStartInstrumentation != null) {
            startSpan(ACTIVITY_CREATION_SPAN_NAME_KEY, appStartInstrumentation.getSpan());
        }
    }

    @Override
    Span createSpan(String spanName, Span parent) {
        final SpanBuilder spanBuilder = tracer.spanBuilder(spanName);
        spanBuilder.setSpanKind(SpanKind.CLIENT);
        //set activity related attributes here
         if (parent != null) {
             spanBuilder.setParent(parent.storeInContext(Context.current()));
         }
        spanBuilder.setAttribute(ACTIVITY_NAME_KEY, activity.getClass().getName());
        return spanBuilder.startSpan();

    }
}
