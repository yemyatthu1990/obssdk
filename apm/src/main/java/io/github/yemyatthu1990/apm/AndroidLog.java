package io.github.yemyatthu1990.apm;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.github.yemyatthu1990.apm.Agent;
import io.github.yemyatthu1990.apm.BuildConfig;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

public class AndroidLog{
    private static final String logKey = "android.log";

    private static String getLogLevel(int level) {
        switch (level) {
            case 2: return "verbose";
            case 3: return "debug";
            case 4: return "info";
            case 5: return "warn";
            case 6: return "error";
            case 7: return "assert";
            default: return "unknown";
        }
    }
    private static void traceLog(int logLevel, String tag, String message) {
        traceLog(logLevel, tag, message, null);
    }


    private static void traceLog(int logLevel, String tag, String message, Throwable t) {
        Tracer tracer = GlobalOpenTelemetry.getTracer(BuildConfig.LIBRARY_NAME, BuildConfig.VERSION_NAME);
        SpanBuilder spanBuilder = tracer.spanBuilder(logKey)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("tag" , tag)
                .setAttribute("message", message)
                .setAttribute("log.level", getLogLevel(logLevel));
                if (t != null) {
                    if (t.getMessage() != null) {
                        spanBuilder.setAttribute(SemanticAttributes.EXCEPTION_MESSAGE, t.getMessage());
                    }
                    StringWriter stringWriter = new StringWriter();
                    t.printStackTrace(new PrintWriter(stringWriter));
                    spanBuilder.setAttribute(SemanticAttributes.EXCEPTION_STACKTRACE, stringWriter.toString());
                    spanBuilder.setAttribute(SemanticAttributes.EXCEPTION_ESCAPED, false);
                }
                Span span = spanBuilder.startSpan();
                span.end();

    }

    public static int d(String tag, String msg) {
        traceLog(Log.DEBUG, tag, msg);
        return Log.d(tag,msg);
    }


    public static int d(String tag, String msg, Throwable t) {
        traceLog(Log.DEBUG, tag, msg,t);
        return Log.d(tag,msg,t);
    }


    public static int e(String tag, String msg) {
        traceLog(Log.ERROR, tag, msg);
        return Log.e(tag,msg);
    }


    public static int e(String tag, String msg, Throwable t) {
        traceLog(Log.ERROR, tag, msg, t);
        return Log.e(tag,msg,t);
    }


    public static int i(String tag, String msg) {
        traceLog(Log.INFO, tag, msg);
        return Log.i(tag,msg);
    }


    public static int i(String tag, String msg, Throwable t) {
        traceLog(Log.INFO, tag, msg,t);
        return Log.i(tag,msg,t);
    }


    public static int v(String tag, String msg) {
        traceLog(Log.VERBOSE, tag, msg);
        return Log.v(tag,msg);
    }


    public static int v(String tag, String msg, Throwable t) {
        traceLog(Log.VERBOSE, tag, msg, t);
        return Log.v(tag,msg,t);
    }


    public static int w(String tag, String msg) {
        traceLog(Log.WARN, tag, msg);
        return Log.w(tag,msg);
    }


    public static int w(String tag, String msg, Throwable t) {
        traceLog(Log.WARN, tag, msg, t);
        return Log.w(tag,msg,t);
    }


    public static int wtf(String tag, String msg) {
        traceLog(Log.ASSERT, tag, msg);
        return Log.wtf(tag,msg);
    }


    public static int wtf(String tag, String msg, Throwable t) {
        traceLog(Log.ASSERT, tag, msg, t);
        return Log.wtf(tag,msg,t);
    }
}
