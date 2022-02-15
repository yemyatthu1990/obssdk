package io.github.yemyatthu1990.apm;

import io.opentelemetry.api.trace.Span;

public class Tracing {
    private Span span;
    private Tracing(Span span) {
        this.span = span;
    }

    static Tracing fromSpan(Span span) {
        return new Tracing(span);
    }
    public String getId() {
        if (this.span != null) return span.getSpanContext().getSpanId();
        else return "";
    }
    public void stopTracing() {
        if (this.span != null) {
            this.span.end();
            this.span = null;
        }
    }
}
