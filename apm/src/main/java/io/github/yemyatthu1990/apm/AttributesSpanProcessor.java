package io.github.yemyatthu1990.apm;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import io.github.yemyatthu1990.apm.collector.DeviceMetricsCollector;
import io.github.yemyatthu1990.apm.collector.MetricsCollector;
import io.github.yemyatthu1990.apm.collector.NetworkMetricCollector;
import io.github.yemyatthu1990.apm.collector.RuntimeAttributesCollector;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class AttributesSpanProcessor implements SpanProcessor {
    private DeviceMetricsCollector deviceMetricsCollector;
    private NetworkMetricCollector networkMetricCollector;
    private RuntimeAttributesCollector runtimeAttributesCollector;
    private ConcurrentMap<String, String> attributes;
    private ThreadPoolExecutor workerThreadPool;
    private AtomicBoolean isShutDown = new AtomicBoolean(false);
    private static final String WORKER_THREAD_NAME =
            AttributesSpanProcessor.class.getSimpleName() + "_WorkerThread";
    public AttributesSpanProcessor(DeviceMetricsCollector deviceMetricsCollector, NetworkMetricCollector networkMetricCollector,
                                   RuntimeAttributesCollector runtimeAttributesCollector) {
        this.deviceMetricsCollector = deviceMetricsCollector;
        this.networkMetricCollector = networkMetricCollector;
        this.runtimeAttributesCollector = runtimeAttributesCollector;
        attributes = new ConcurrentHashMap<>();
        //At most, we only have three collector services

        workerThreadPool = new ThreadPoolExecutor(1,3,1, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),new DaemonThreadFactory(WORKER_THREAD_NAME));

    }

    @SuppressLint("NewApi")
    @Override
    public void onStart(@NonNull Context parentContext, @NonNull ReadWriteSpan span) {
        CompletableFuture<Void> deviceMetricFuture =
                CompletableFuture.runAsync(new Worker(attributes, deviceMetricsCollector), workerThreadPool);
        CompletableFuture<Void> networkMetricFuture =
                CompletableFuture.runAsync(new Worker(attributes, networkMetricCollector), workerThreadPool);
        CompletableFuture<Void> runtimeMetricFuture =
                CompletableFuture.runAsync(new Worker(attributes, runtimeAttributesCollector), workerThreadPool);
        try {
            //Shouldn't take more than 1 second to collect all these attributes.
            CompletableFuture.allOf(deviceMetricFuture, networkMetricFuture, runtimeMetricFuture)
                    .get(1, TimeUnit.SECONDS);
            attributes.forEach(span::setAttribute);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {

    }

    @Override
    public CompletableResultCode shutdown() {
        if (isShutDown.getAndSet(true)) {
            return CompletableResultCode.ofSuccess();
        }
        workerThreadPool.shutdown();
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }

    static class Worker implements Runnable {
        private final ConcurrentMap<String, String> attributes;
        private final MetricsCollector collector;
        public Worker(ConcurrentMap<String, String> attributes, MetricsCollector metricsCollector) {
            this.attributes = attributes;
            this.collector = metricsCollector;

        }
        @Override
        public void run() {
            this.attributes.putAll(collector.getMetric());
        }
    }
}
