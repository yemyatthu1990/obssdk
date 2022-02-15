package io.github.yemyatthu1990.apm;

import android.os.Handler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

class ANRReporter implements Runnable, AppState {
    private final AtomicInteger anrCounter = new AtomicInteger();
    private final Handler uiHandler;
    private final Thread mainThread;
    private final ScheduledExecutorService anrScheduler;
    private ScheduledFuture<?> scheduledFuture;
    ANRReporter(Handler uiHandler, Thread mainThread) {
        this.uiHandler = uiHandler;
        this.mainThread = mainThread;
         anrScheduler = Executors.newScheduledThreadPool(1);
         scheduledFuture = anrScheduler.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        CountDownLatch response = new CountDownLatch(1);
        if (!uiHandler.post(response::countDown)) {
            //the main thread is probably shutting down. ignore and return.
            return;
        }
        boolean success;
        try {
            success = response.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return;
        }
        if (success) {
            anrCounter.set(0);
            return;
        }
        if (anrCounter.incrementAndGet() >= 5) {
            StackTraceElement[] stackTrace = mainThread.getStackTrace();

            Agent.getTracer()
                    .spanBuilder("anr")
                    .setSpanKind(SpanKind.CLIENT)
                    .setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE,
                            Utils.formatStackTrace(stackTrace))
                    .setAttribute(AgentConstant.COMPONENT_TYPE , AgentConstant.COMPONENT.ERROR.name())

                    .startSpan()
                    .setStatus(StatusCode.ERROR)
                    .end();
            //only report once per 5s.
            anrCounter.set(0);
        }
    }

    @Override
    public void onAppEnterBackground() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    @Override
    public void onAppEnterForeground() {
        if (scheduledFuture == null) {
            scheduledFuture = anrScheduler.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
        }
    }
}
