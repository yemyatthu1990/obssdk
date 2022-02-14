package io.github.yemyatthu1990.apm.instrumentations;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

public class StdoutMetricsExporter implements MetricExporter {
    @Nullable
    @Override
    public AggregationTemporality getPreferredTemporality() {
        return MetricExporter.super.getPreferredTemporality();
    }

    @Override
    public CompletableResultCode export(Collection<MetricData> metrics) {

        System.out.println("___________Metric data_________");
        for (MetricData metricData: metrics) {


            for( DoublePointData doublePointData:  metricData.getDoubleGaugeData().getPoints()) {
                System.out.println("Metrics double gauge data: "+doublePointData);
            }
            for( DoubleHistogramPointData doublePointData:  metricData.getDoubleHistogramData().getPoints()) {
                System.out.println("Metrics double histogram data: "+doublePointData);
            }
            System.out.println("Metrics name: "+metricData.getName());
            System.out.println("Metrics description: "+metricData.getDescription());
            System.out.println("Metrics unit: "+metricData.getUnit());
            Map<AttributeKey<?>, Object> attributeKeyStringMap = metricData.getResource().getAttributes().asMap();
            for (AttributeKey<?> key: attributeKeyStringMap.keySet()) {
                System.out.println("Metrics Resources "+ key + " : "+ attributeKeyStringMap.get(key));
            }
        }
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
}
