package io.github.yemyatthu1990.apm;

import android.annotation.SuppressLint;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.function.BiConsumer;

import io.github.yemyatthu1990.apm.collectors.CpuInfo;
import io.github.yemyatthu1990.apm.collectors.DeviceMetricsCollector;
import io.github.yemyatthu1990.apm.collectors.MemoryInfo;
import io.github.yemyatthu1990.apm.collectors.NetworkMetricCollector;
import io.github.yemyatthu1990.apm.collectors.RuntimeAttributesCollector;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class AttributesSpanProcessor implements SpanProcessor {
    private DeviceMetricsCollector deviceMetricsCollector;
    private NetworkMetricCollector networkMetricCollector;
    private RuntimeAttributesCollector runtimeAttributesCollector;

    public AttributesSpanProcessor(DeviceMetricsCollector deviceMetricsCollector, NetworkMetricCollector networkMetricCollector,
                                   RuntimeAttributesCollector runtimeAttributesCollector) {
        this.deviceMetricsCollector = deviceMetricsCollector;
        this.networkMetricCollector = networkMetricCollector;
        this.runtimeAttributesCollector = runtimeAttributesCollector;
    }

    @SuppressLint("NewApi")
    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        this.deviceMetricsCollector.getDeviceMetrics().forEach(span::setAttribute);
        this.networkMetricCollector.getNetworkMetrics().forEach(span::setAttribute);
        this.runtimeAttributesCollector.getRuntimeAttributes().forEach(span::setAttribute);
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {

    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
}
