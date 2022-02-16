package io.github.yemyatthu1990.apm;

interface SessionIdChangeListener {
    void onChange(String oldSessionId, String newSessionId);
}
