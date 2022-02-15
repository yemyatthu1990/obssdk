package io.github.yemyatthu1990.apm;

import io.opentelemetry.api.trace.Tracer;

class SessionTracer implements SessionIdChangeListener{
    private final Tracer tracer;
    SessionTracer(Tracer tracer) {
        this.tracer = tracer;
    }
    @Override
    public void onChange(String oldSessionId, String newSessionId) {
        String previousSessionIdKey = "session.id.old";
        tracer.spanBuilder("session.id.change")
                .setAttribute(previousSessionIdKey, oldSessionId)
                .startSpan()
                .end();
    }
}
