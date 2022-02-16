package io.github.yemyatthu1990.apm.reporter;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.github.yemyatthu1990.apm.AgentConstant;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class CrashReporter {
    public static void initializeCrashReporting(Tracer tracer, SdkTracerProvider tracerProvider) {
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new CrashReportHandler(tracer,tracerProvider,defaultHandler));
    }

    static class CrashReportHandler implements Thread.UncaughtExceptionHandler {
        private final Tracer tracer;
        private final Thread.UncaughtExceptionHandler defaultHandler;
        private final SdkTracerProvider tracerProvider;
        public CrashReportHandler(Tracer tracer, SdkTracerProvider tracerProvider,  Thread.UncaughtExceptionHandler defaultHandler) {
            this.tracer = tracer;
            this.defaultHandler = defaultHandler;
            this.tracerProvider = tracerProvider;
        }
        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            StringWriter crashWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(crashWriter));
            Span span = tracer.spanBuilder(e.getClass().getSimpleName())
                    .setSpanKind(SpanKind.CLIENT)
                    .setAttribute(AgentConstant.COMPONENT_TYPE, AgentConstant.component.error.name())
                    .setAttribute(SemanticAttributes.THREAD_ID, t.getId())
                    .setAttribute(SemanticAttributes.THREAD_NAME, t.getName())
                    .setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE, crashWriter.toString())
                    .setAttribute(SemanticAttributes.EXCEPTION_ESCAPED, true)
                    .startSpan();
            span.setStatus(StatusCode.ERROR).end();
            tracerProvider.forceFlush();
            if (defaultHandler != null) defaultHandler.uncaughtException(t, e);
        }
    }
}
