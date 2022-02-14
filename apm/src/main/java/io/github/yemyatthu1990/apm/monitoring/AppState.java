package io.github.yemyatthu1990.apm.monitoring;

public interface AppState {
    void onAppEnterBackground();
    void onAppEnterForeground();
}
