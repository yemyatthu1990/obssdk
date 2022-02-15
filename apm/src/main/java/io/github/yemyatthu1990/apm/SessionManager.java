package io.github.yemyatthu1990.apm;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.Clock;

class SessionManager {
    private static final long sessionDuration = TimeUnit.HOURS.toNanos(12);

        private final Clock clock;
        private final AtomicReference<String> value = new AtomicReference<>();
        private volatile long createTimeNanos;
        private volatile SessionIdChangeListener sessionIdChangeListener;

        SessionManager() {
            this(Clock.getDefault());
        }

        SessionManager(Clock clock) {
            this.clock = clock;
            value.set(createNewId());
            createTimeNanos = clock.now();
        }

        private static String createNewId() {
            return UUID.randomUUID().toString().replace("[^a-zA-Z0-9]", "");
        }

        String getSessionId() {
            String currentValue = value.get();
            if (sessionExpired()) {
                String newId = createNewId();
                //if this returns false, then another thread updated the value already.
                if (value.compareAndSet(currentValue, newId)) {
                    createTimeNanos = clock.now();
                    if (sessionIdChangeListener != null) {
                        sessionIdChangeListener.onChange(currentValue, newId);
                    }
                }
                return value.get();
            }
            return currentValue;
        }

        void setSessionIdChangeListener(SessionIdChangeListener sessionIdChangeListener) {
            this.sessionIdChangeListener = sessionIdChangeListener;
        }

        private boolean sessionExpired() {
            long elapsedTime = clock.now() - createTimeNanos;
            return elapsedTime >= sessionDuration;
        }
}
