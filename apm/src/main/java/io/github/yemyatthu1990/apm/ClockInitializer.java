package io.github.yemyatthu1990.apm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

import io.opentelemetry.sdk.common.Clock;

public class ClockInitializer implements Initializer<Clock> {
    @NonNull
    @Override
    public Clock create(@NonNull Context context) {
        return Clock.getDefault();
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
