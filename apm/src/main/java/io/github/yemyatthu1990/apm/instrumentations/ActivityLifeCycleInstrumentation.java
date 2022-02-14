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

import java.util.HashMap;
import java.util.Map;

import io.github.yemyatthu1990.apm.log.ActivityTraceLogger;
import io.github.yemyatthu1990.apm.log.TraceLogger;
import io.github.yemyatthu1990.apm.monitoring.AppState;
import io.opentelemetry.api.GlobalOpenTelemetry;

class ActivityLifeCycleInstrumentation implements Application.ActivityLifecycleCallbacks {
    private int activitiesCount = 0;

    //Keep track of TraceLogger instances for every activity in the app
    private final Map<String, TraceLogger> activityTraceLoggers;

    private final AppState appState;
    public ActivityLifeCycleInstrumentation(AppState appState) {
        this.appState = appState;
        this.activityTraceLoggers = new HashMap<>();
    }
    private TraceLogger startSpan(Activity activity, String event) {
        String activityName = activity.getClass().getName();
        String spanName = activityName+"."+event;
        TraceLogger traceLogger;
        if (activityTraceLoggers.containsKey(activityName)) {
            traceLogger = activityTraceLoggers.get(activityName);
        }
        else {
            traceLogger = new ActivityTraceLogger(GlobalOpenTelemetry.getTracer(
                    "Fragment","0.0.1"
            ), activity);
            activityTraceLoggers.put(activityName, traceLogger);
        }
        assert traceLogger != null;
        return traceLogger
                .startSpan(spanName);
    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        startSpan(activity, "onActivityPreCreated");
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        startSpan(activity, "onActivityCreated");
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (activitiesCount == 0) {
            if (appState != null) appState.onAppEnterForeground();
        }
        activitiesCount++;
        startSpan(activity, "onActivityStarted");
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        startSpan(activity, "onActivityResumed");
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        startSpan(activity, "onActivityPaused");
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (--activitiesCount==0){
            if (appState != null) appState.onAppEnterForeground();
        }
        startSpan(activity, "onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        startSpan(activity, "onActivitySavedInstanceState");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        startSpan(activity, "onActivityDestroyed")
                .endActiveSpan();
    }
}
