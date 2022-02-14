package io.github.yemyatthu1990.apm.instrumentations;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.HashMap;
import java.util.Map;

import io.github.yemyatthu1990.apm.BuildConfig;
import io.github.yemyatthu1990.apm.log.FragmentTraceLogger;
import io.github.yemyatthu1990.apm.log.TraceLogger;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;

public class FragmentLifeCycleInstrumentation extends FragmentManager.FragmentLifecycleCallbacks {

    private final Map<String, TraceLogger> fragmentTraceLoggers;

    public FragmentLifeCycleInstrumentation() {
        fragmentTraceLoggers = new HashMap<>();
    }

    private TraceLogger startSpan(Fragment fragment, String event) {
        String fragmentName = fragment.getClass().getName();
        String spanName = fragmentName+"."+event;
        TraceLogger traceLogger;
        if (fragmentTraceLoggers.containsKey(fragmentName)) {
             traceLogger = fragmentTraceLoggers.get(fragmentName);
        }
        else {
             traceLogger = new FragmentTraceLogger(GlobalOpenTelemetry.getTracer(
                    "Fragment", BuildConfig.VERSION_NAME
            ), fragment);
            fragmentTraceLoggers.put(fragmentName, traceLogger);
        }
        assert traceLogger != null;
        return traceLogger
                .startSpan(spanName, null);
    }
    @Override
    public void onFragmentCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @Nullable Bundle savedInstanceState) {
        super.onFragmentCreated(fm, f, savedInstanceState);
        startSpan(f, "onFragmentCreated");
    }

    @Override
    public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
        super.onFragmentAttached(fm, f, context);
        startSpan(f, "onFragmentAttached");
    }

    @Override
    public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentDetached(fm, f);
        startSpan(f, "onFragmentDetached");
    }

    @Override
    public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentPaused(fm, f);
        startSpan(f, "onFragmentPaused");
    }

    @Override
    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentResumed(fm, f);
        startSpan(f, "onFragmentResumed");
    }

    @Override
    public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentStarted(fm, f);
        startSpan(f, "onFragmentStarted");
    }

    @Override
    public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentStopped(fm, f);
        startSpan(f, "onFragmentStopped");
    }

    @Override
    public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        super.onFragmentDestroyed(fm, f);
        startSpan(f, "onFragmentDestroyed")
                .endActiveSpan();
    }
}
