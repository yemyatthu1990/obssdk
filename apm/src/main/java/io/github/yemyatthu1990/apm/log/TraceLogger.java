package io.github.yemyatthu1990.apm.log;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

public abstract class TraceLogger {
    protected final Tracer tracer;
    protected final ActiveSpan activeSpan;
    public TraceLogger(Tracer tracer) {
        this.tracer = tracer;
        this.activeSpan = new ActiveSpan();
    }

    public TraceLogger startSpan(String spanName) {
        //If there is a span in progress, dont' do anything
        if (activeSpan.spanInProgress()) {
            return this;
        }
        activeSpan.startSpan(() -> createSpan(spanName));
        return this;
    }

    public void endActiveSpan() {
        activeSpan.endActiveSpan();
    }

    public void addEvent(String eventName) {
        activeSpan.addEvent(eventName);
    }

    abstract Span createSpan(String spanName);

}
