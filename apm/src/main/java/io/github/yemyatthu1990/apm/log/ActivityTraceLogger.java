package io.github.yemyatthu1990.apm.log;

import android.app.Activity;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

public class ActivityTraceLogger extends TraceLogger{
    static final AttributeKey<String> ACTIVITY_NAME_KEY = AttributeKey.stringKey("activityName");
    private final Activity activity;
    public ActivityTraceLogger(Tracer tracer, Activity activity) {
        super(tracer);
        this.activity = activity;
    }

    @Override
    Span createSpan(String spanName) {
        final SpanBuilder spanBuilder = tracer.spanBuilder(spanName);
        spanBuilder.setSpanKind(SpanKind.CLIENT);
        //set activity related attributes here
        spanBuilder.setAttribute(ACTIVITY_NAME_KEY, activity.getClass().getName());
        return spanBuilder.startSpan();

    }
}
