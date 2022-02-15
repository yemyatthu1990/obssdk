/*
 * Copyright Splunk Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.yemyatthu1990.apm.instrumentations;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.github.yemyatthu1990.apm.BuildConfig;
import io.github.yemyatthu1990.apm.log.ActivityTraceLogger;
import io.github.yemyatthu1990.apm.log.TraceLogger;
import io.github.yemyatthu1990.apm.monitoring.AppState;
import io.opentelemetry.api.GlobalOpenTelemetry;

public class ActivityLifeCycleInstrumentation implements Application.ActivityLifecycleCallbacks {
    private int activitiesCount = 0;

    //Keep track of TraceLogger instances for every activity in the app
    private final Map<String, ActivityTraceLogger> activityTraceLoggers;
    private AppStartInstrumentation appStartInstrumentation ;
    private AtomicReference<String> initialActivity = new AtomicReference<>();
    private final AppState appState;
    public ActivityLifeCycleInstrumentation(AppState appState, AppStartInstrumentation appStartInstrumentation) {
        this.appState = appState;
        this.appStartInstrumentation = appStartInstrumentation;
        this.activityTraceLoggers = new HashMap<>();
    }
    private ActivityTraceLogger getTracer(Activity activity) {
        ActivityTraceLogger traceLogger;
        String activityName = activity.getClass().getName();
        if (activityTraceLoggers.containsKey(activityName)) {
            traceLogger = activityTraceLoggers.get(activityName);
        }
        else {
            traceLogger = new ActivityTraceLogger(GlobalOpenTelemetry.getTracerProvider().get(
                    "ActivityIntrumentation", BuildConfig.VERSION_NAME
            ), activity, appStartInstrumentation);
            activityTraceLoggers.put(activityName, traceLogger);
        }
        return traceLogger;
    }

    private void startActivityCreationSpan(Activity activity) {
        if (initialActivity.get() == null) {
            initialActivity.set(activity.getClass().getName());
            getTracer(activity)
                    .startAppstartActivityCreationSpan();
        } else {
            getTracer(activity)
                    .startNormalActivityCreationSpan();
        }
    }

    private void startSpanIfNoneInProgress(Activity activity, String spanName) {
        getTracer(activity)
                .startSpan(spanName, null);
    }
    private void addEvent(Activity activity, String eventName) {
        getTracer(activity)
                .addEvent(eventName);
    }

    private void endSpan(Activity activity) {
        getTracer(activity)
                .endActiveSpan();
    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        startActivityCreationSpan(activity);
        addEvent(activity, "onActivityPreCreated");

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        addEvent(activity, "onActivityCreated");
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        addEvent(activity, "onActivityPostCreated");
        endSpan(activity);
        if (appStartInstrumentation.getSpan()!=null) {
            //Stop app startup
            appStartInstrumentation.end();
        }

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (activitiesCount == 0) {
            if (appState != null) appState.onAppEnterForeground();
        }
        activitiesCount++;
        if (activity instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            fragmentManager.registerFragmentLifecycleCallbacks(new FragmentLifeCycleInstrumentation(), true);
        }
    }

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {
        startSpanIfNoneInProgress(activity, "Activity Resumed");
        addEvent(activity, "onActivityPreResumed");
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        addEvent(activity, "onActivityResumed");
    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
        addEvent(activity, "onActivityPostResumed");
        endSpan(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (--activitiesCount==0){
            if (appState != null) appState.onAppEnterForeground();
        }

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
