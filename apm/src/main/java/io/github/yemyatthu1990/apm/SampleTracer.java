package io.github.yemyatthu1990.apm;

import java.util.Map;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.context.Context;

public class SampleTracer {
    private Span span;
    private SampleTracer(Span span) {
        this.span = span;
    }

    static SampleTracer fromSpan(Span span) {
        return new SampleTracer(span);
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

    /**
     * Add a child sample tracer for the ongoing trace
     * @param transactionName name of the child tracer
     * @param attributes custom attributes
     * @return a @SampleTracer
     */
    public SampleTracer addTrace(String transactionName, Map<String, String> attributes){
        SpanBuilder builder = Agent.getTracer().spanBuilder(transactionName);
        if (attributes != null) {
            attributes.forEach(builder::setAttribute);
        }
        Span childSpan = builder
                .setParent(this.span.storeInContext(Context.current()))
                .startSpan();
        return new SampleTracer(childSpan);
    }
}
