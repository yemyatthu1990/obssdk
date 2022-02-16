package io.github.yemyatthu1990.apm;

import java.util.concurrent.atomic.AtomicBoolean;

import io.github.yemyatthu1990.apm.AppState;
import io.github.yemyatthu1990.apm.SessionIdChangeListener;
import io.opentelemetry.api.trace.Tracer;

class SessionTracer implements SessionIdChangeListener, AppState {
    private final Tracer tracer;
    private final AtomicBoolean shouldEmitChange = new AtomicBoolean(true);
    SessionTracer(Tracer tracer) {
        this.tracer = tracer;
    }
    @Override
    public void onChange(String oldSessionId, String newSessionId) {
        if (shouldEmitChange.get()) {
            String previousSessionIdKey = "session.id.old";
            tracer.spanBuilder("session.id.change")
                    .setAttribute(previousSessionIdKey, oldSessionId)
                    .startSpan()
                    .end();
        }
    }

    @Override
    public void onAppEnterBackground() {
        shouldEmitChange.set(false);
    }

    @Override
    public void onAppEnterForeground() {
        shouldEmitChange.set(true);
    }
}
