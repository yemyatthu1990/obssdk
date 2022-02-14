package io.github.yemyatthu1990.apm.monitoring;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.util.function.BiConsumer;

import io.github.yemyatthu1990.apm.collectors.DeviceMetricsCollector;
import io.github.yemyatthu1990.apm.collectors.Mapper;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;

public class AgentResource {
    public static Resource get(Context context) {
        Resource defaultResources =  Resource.getDefault().toBuilder()
                .put("service.name", getApplicationName(context))
                .put("telemetry.sdk.name", "elastic-android")
                .put("telemetry.sdk.version", "0.0.1")
                .build();
        DeviceMetricsCollector deviceMetricsCollector = new DeviceMetricsCollector(context);
        AttributesBuilder builder = Attributes.builder();
        deviceMetricsCollector.getDeviceMetrics().forEach((key, value) -> {
            builder.put(AttributeKey.stringKey(key), value);
        });
        Attributes attributes =  builder.build();
        Resource resourceToBeMerged = Resource.create(attributes);
        return defaultResources.merge(resourceToBeMerged);
    }

    private static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

}
