package io.github.yemyatthu1990.apm;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import io.github.yemyatthu1990.apm.collector.DeviceMetricsCollector;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;

class AgentResource {
    static Resource get(Context context) {
        return Resource.getDefault().toBuilder()
                .put("service.name", getApplicationName(context))
                .put("telemetry.sdk.name", BuildConfig.LIBRARY_NAME)
                .put("telemetry.sdk.version", BuildConfig.VERSION_NAME)
                .build();
    }

    static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

}
