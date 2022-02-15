package io.github.yemyatthu1990.apm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class MetricsCollector implements Mapper {
    private final ConcurrentMap<String, String> cache;

    public MetricsCollector() {
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public boolean hasKey(@NonNull String key) {
        return cache.containsKey(key);
    }

    @Nullable
    @Override
    public String getValue(@NonNull String key) {
        return cache.get(key);
    }

    @Nullable
    @Override
    public ConcurrentMap<String, String> map() {
        return cache;
    }

    @Nullable
    @Override
    public Set<String> getKeySet() {
        return cache.keySet();
    }

    @Nullable
    @Override
    public Collection<String> valueSet() {
        return cache.values();
    }

    @Override
    public void put(@NonNull String key, @NonNull String value) {
        cache.put(key, value);
    }

    @Override
    public void putAll(@Nullable ConcurrentMap<String, String> map) {
        if (map != null) {
            cache.putAll(map);
        }
    }

    @Override
    public void clear() {
        cache.clear();
    }
}