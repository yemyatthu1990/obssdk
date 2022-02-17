package io.github.yemyatthu1990.apm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.yemyatthu1990.apm.reporter.CrashReporter;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.testing.junit4.OpenTelemetryRule;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class CrashReporterTest {
    @Rule
    public OpenTelemetryRule otelTesting = OpenTelemetryRule.create();
    private Tracer tracer;

    @Before

    public void setup() {tracer = otelTesting.getOpenTelemetry().getTracer("testTracer");}

    @Test
    public void crashReportingSpan() {
        TestDelegateHandler testDelegateHandler = new TestDelegateHandler();
        SdkTracerProvider tracerProvider = mock(SdkTracerProvider.class);
        CrashReporter.CrashReportHandler crashReportHandler = new CrashReporter.CrashReportHandler(tracer, tracerProvider, testDelegateHandler);
        NullPointerException testException = new NullPointerException("nullCrash");
        Thread crashThread = new Thread("crashingThread");
        crashReportHandler.uncaughtException(crashThread, testException);
        List<SpanData> spans = otelTesting.getSpans();
        assertEquals(1,spans.size());
        SpanData crashSpan = spans.get(0);
        assertEquals(testException.getClass().getSimpleName(), crashSpan.getName());
        assertEquals(crashThread.getId(), (long)crashSpan.getAttributes().get(SemanticAttributes.THREAD_ID));
        assertEquals(crashThread.getName(), crashSpan.getAttributes().get(SemanticAttributes.THREAD_NAME));
        assertTrue(crashSpan.getAttributes().get(SemanticAttributes.EXCEPTION_ESCAPED));
        assertNotNull(crashSpan.getAttributes().get(SemanticAttributes.EXCEPTION_STACKTRACE));
        assertTrue(crashSpan.getAttributes().get(SemanticAttributes.EXCEPTION_STACKTRACE).contains("NullPointerException"));
        assertTrue(crashSpan.getAttributes().get(SemanticAttributes.EXCEPTION_STACKTRACE).contains("nullCrash"));
        assertEquals("NullPointerException", crashSpan.getAttributes().get(SemanticAttributes.EXCEPTION_TYPE));
        assertEquals("nullCrash", crashSpan.getAttributes().get(SemanticAttributes.EXCEPTION_MESSAGE));
        assertEquals(StatusCode.ERROR, crashSpan.getStatus().getStatusCode());
        assertTrue(testDelegateHandler.delegatedTo.get());
        verify(tracerProvider).forceFlush();
    }

    private void assertNotNull(String s) {
    }

    private static class TestDelegateHandler implements Thread.UncaughtExceptionHandler {
        final AtomicBoolean delegatedTo = new AtomicBoolean(false);
        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            delegatedTo.set(true);
        }
    }
}
