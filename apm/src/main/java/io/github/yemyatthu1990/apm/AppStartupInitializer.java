package io.github.yemyatthu1990.apm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import java.util.ArrayList;
import java.util.List;

import io.github.yemyatthu1990.apm.instrumentation.AppStartInstrumentation;
import io.opentelemetry.sdk.common.Clock;

public class AppStartupInitializer implements Initializer<AppStartInstrumentation> {
    @NonNull
    @Override
    public AppStartInstrumentation create(@NonNull Context context) {
        AppStartInstrumentation.init(Clock.getDefault().now());
        return  AppStartInstrumentation.getInstance();
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        List<Class<? extends Initializer<?>>> classes = new ArrayList<>();
        classes.add(ClockInitializer.class);
        return classes;
    }
}
