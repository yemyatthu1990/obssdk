package io.github.yemyatthu1990.apm;

interface AppState {
    void onAppEnterBackground();
    void onAppEnterForeground();
}
