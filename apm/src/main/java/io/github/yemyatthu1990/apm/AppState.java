package io.github.yemyatthu1990.apm;

public interface AppState {
    void onAppEnterBackground();
    void onAppEnterForeground();
}
