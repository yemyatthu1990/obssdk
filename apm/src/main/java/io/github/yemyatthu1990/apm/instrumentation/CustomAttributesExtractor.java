package io.github.yemyatthu1990.apm.instrumentation;

import android.content.Context;

import androidx.annotation.Nullable;

import io.github.yemyatthu1990.apm.BuildConfig;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import okhttp3.Request;
import okhttp3.Response;

public class CustomAttributesExtractor implements AttributesExtractor<Request, Response> {
    public CustomAttributesExtractor() {
    }
    @Override
    public void onStart(AttributesBuilder attributes, Request request) {
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Request request, @Nullable Response response, @Nullable Throwable error) {

    }

    @Override
    public <T> void set(AttributesBuilder attributes, AttributeKey<T> key, @Nullable T value) {
        if (key.getKey().equals("otel.library.name")) {
            AttributesExtractor.super.set(attributes, key, (T)BuildConfig.LIBRARY_NAME);
        } else {
            AttributesExtractor.super.set(attributes, key, value);
        }
    }
}
