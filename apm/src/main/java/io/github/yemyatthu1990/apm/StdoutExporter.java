package io.github.yemyatthu1990.apm;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.github.yemyatthu1990.apm.Utils;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

class StdoutExporter implements SpanExporter {
    public StdoutExporter() {
    }
    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
//        if (isDebug) {
            for (SpanData span: spans) {
                SpanExporterData spanExporterData = new SpanExporterData(span);
                System.out.println("___________________");
                System.out.println("Span "+ spanExporterData.span);
                System.out.println("Parent Span ID "+ spanExporterData.parentSpanId);
                System.out.println("Span ID "+ spanExporterData.spanId);
                System.out.println("Span Kind "+ spanExporterData.spanKind);
                System.out.println("Trace ID "+ spanExporterData.traceId);
                System.out.println("Span duration "+ spanExporterData.duration);
                System.out.println("Span start "+ spanExporterData.start);
                for (AttributeKey<?> attributeKey: spanExporterData.attributes.keySet()) {
                    System.out.println("Span Attributes: "+attributeKey.getKey() + " : "+spanExporterData.attributes.get(attributeKey));
                }
                for (String string: spanExporterData.events) {
                    System.out.println("Span event : "+ string);
                }
                System.out.println("Span trace flags "+ spanExporterData.traceFlags);
                for (String key: spanExporterData.traceState.keySet()) {
                    System.out.println("Span trace states: "+ key+" : "+ spanExporterData.traceState.get(key));
                }
            }
//        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public void close() {
        SpanExporter.super.close();
    }

    static class SpanExporterData implements Serializable {
        private String span;
        private String traceId;
        private String spanId;
        private String spanKind;
        private String traceFlags;
        private Map<String, String> traceState;
        private String parentSpanId;
        private Long start;
        private long duration;
        private Map<AttributeKey<?>, Object> attributes;
        private List<String> events;

        public SpanExporterData( SpanData span) {

            this.span = span.getName();
            traceId = Utils.toHex(span.getTraceId());
            spanId = Utils.toHex(span.getSpanId());
            spanKind = span.getKind().name();
            traceFlags = span.getSpanContext().getTraceFlags().asHex();
            traceState = span.getSpanContext().getTraceState().asMap();
            if (span.getParentSpanId() != null && span.getParentSpanId().toString().length() > 0) {
                parentSpanId = Utils.toHex(span.getParentSpanId());
            } else {
                parentSpanId = Utils.toHex(SpanId.getInvalid());
            }
            if (span.getEvents() != null) {
                events = new ArrayList<>();
                for (EventData eventData: span.getEvents()) {
                    events.add(eventData.getName());
                }
            }

            start = span.getStartEpochNanos();
            duration = span.getEndEpochNanos() - span.getStartEpochNanos();
            attributes = span.getAttributes().asMap();
        }
    }
}
