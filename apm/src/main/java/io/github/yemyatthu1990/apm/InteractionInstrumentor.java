package io.github.yemyatthu1990.apm;

import javax.annotation.Nullable;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

public interface InteractionInstrumentor {
    void start(Tracer tracer);
    void end();
    @Nullable
    Span getSpan();
}
