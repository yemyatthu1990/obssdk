package io.github.yemyatthu1990.apm;

import androidx.fragment.app.Fragment;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

public class FragmentTraceLogger extends TraceLogger {
    static final AttributeKey<String> FRAGMENT_NAME_KEY = AttributeKey.stringKey("fragmentName");
    private final Fragment fragment;
    public FragmentTraceLogger(Tracer tracer, Fragment fragment) {
        super(tracer);
        this.fragment = fragment;
    }

    @Override
    Span createSpan(String spanName, Span parent) {
        final SpanBuilder spanBuilder = tracer.spanBuilder(spanName);
        spanBuilder.setSpanKind(SpanKind.CLIENT);
        //set fragment related attributes here
        spanBuilder.setAttribute(FRAGMENT_NAME_KEY, fragment.getClass().getName());
        return spanBuilder.startSpan();
    }
}
