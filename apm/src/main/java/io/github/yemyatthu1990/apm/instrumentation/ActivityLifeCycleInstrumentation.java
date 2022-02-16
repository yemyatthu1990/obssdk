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

package io.github.yemyatthu1990.apm.instrumentation;

import android.app.Activity;
import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.github.yemyatthu1990.apm.AppState;
import io.github.yemyatthu1990.apm.reporter.DeprecatedAPINetworkChangeReporter;

public class ActivityLifeCycleInstrumentation implements Application.ActivityLifecycleCallbacks {
    private int activitiesCount = 0;

    //Keep track of TraceLogger instances for every activity in the app
    private final Map<String, ActivityTraceLogger> activityTraceLoggers;
    private final AppStartInstrumentation appStartInstrumentation ;
    private final AtomicReference<String> initialActivity = new AtomicReference<>();
    private final List<AppState> appStates = new ArrayList<>();
    private final DeprecatedAPINetworkChangeReporter deprecatedAPINetworkChangeReporter;
    public ActivityLifeCycleInstrumentation(List<AppState> appStates, @Nullable DeprecatedAPINetworkChangeReporter deprecatedAPINetworkChangeReporter, AppStartInstrumentation appStartInstrumentation) {
        if (appStates != null) {
            this.appStates.addAll(appStates);
        }
        this.deprecatedAPINetworkChangeReporter = deprecatedAPINetworkChangeReporter;
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
            traceLogger = new ActivityTraceLogger(activity, appStartInstrumentation);
            activityTraceLoggers.put(activityName, traceLogger);
        }
        return traceLogger;
    }


    @Override
    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (initialActivity.get() == null) {
            initialActivity.set(activity.getClass().getName());
            getTracer(activity)
                    .startAppStartActivityCreationSpan();

        } else {
            getTracer(activity)
                    .startNormalActivityCreationSpan();
        }
        getTracer(activity).addEvent("onActivityPreCreated");

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        getTracer(activity).addEvent("onActivityCreated");
    }

    @Override
    public void onActivityPostCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        getTracer(activity).addEvent("onActivityPostCreated");
        getTracer(activity).endActiveSpan();
        if (appStartInstrumentation.getSpan()!=null) {
            //Stop app startup
            appStartInstrumentation.end();
        }
    }

    @Override
    public void onActivityPreStarted(@NonNull Activity activity) {
        if (activitiesCount == 0) {
            for(AppState appState: appStates) {
                appState.onAppEnterForeground();
            }
            getTracer(activity)
                    .startAppForegroundSpan();
            getTracer(activity).addEvent("onActivityPreStarted");
        }
        activitiesCount++;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        activity.registerReceiver(deprecatedAPINetworkChangeReporter, new IntentFilter());
    }


    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (activity instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            fragmentManager.registerFragmentLifecycleCallbacks(new FragmentLifeCycleInstrumentation(), true);
        }
        getTracer(activity).addEvent("onActivityStarted");

    }

    @Override
    public void onActivityPostStarted(@NonNull Activity activity) {
        getTracer(activity).addEvent("onActivityPostStarted");
        getTracer(activity)
                .endActiveSpan();
    }

    @Override
    public void onActivityPostSaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityPreResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPostResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPreStopped(@NonNull Activity activity) {
        if (--activitiesCount==0){
            for(AppState appState: appStates) {
                appState.onAppEnterBackground();
            }
            getTracer(activity)
                    .startAppBackgroundSpan();
            getTracer(activity).addEvent("activityPreStopped");
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        getTracer(activity).addEvent("activityStopped");
    }

    @Override
    public void onActivityPostStopped(@NonNull Activity activity) {
        getTracer(activity).addEvent("activityPostStopped");
        getTracer(activity)
                .endActiveSpan();
    }


    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityPreDestroyed(@NonNull Activity activity) {
        getTracer(activity)
                .startActivityDestroyedSpan();
        getTracer(activity).addEvent("activityPreDestroyed");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        getTracer(activity).addEvent("activityDestroyed");
    }

    @Override
    public void onActivityPostDestroyed(@NonNull Activity activity) {
        getTracer(activity).addEvent("activityPostDestroyed");
        getTracer(activity).endActiveSpan();

    }
}
