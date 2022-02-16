package io.github.yemyatthu1990.apm.instrumentation;

import androidx.annotation.Nullable;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

abstract class TraceLogger {
    protected final Tracer tracer;
    protected final ActiveSpan activeSpan;

    TraceLogger(Tracer tracer) {
        this.tracer = tracer;
        this.activeSpan = new ActiveSpan();
    }
    TraceLogger startSpan(String spanName) {
        return startSpan(spanName, null);
    }

    TraceLogger startSpan(String spanName, Span parent) {
        if (activeSpan.spanInProgress()) {
            return this;
        }
        activeSpan.startSpan(() -> createSpan(spanName, parent));
        return this;
    }

    @Nullable
    Span getUnderlyingSpan() {
        return activeSpan.getUnderlyingSpan();
    }

    void endActiveSpan() {
        activeSpan.endActiveSpan();
    }

    void addEvent(String eventName) {
        activeSpan.addEvent(eventName);
    }

    abstract Span createSpan(String spanName, Span parente);

}
