package io.github.yemyatthu1990.apm;

import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

interface Mapper {
    int size();
    boolean isEmpty();
    boolean hasKey(String key);
    @Nullable
    String getValue(String key);
    ConcurrentMap<String, String> map();
    Set<String> getKeySet();
    Collection<String> valueSet();
    void put( String key, String value);
    void putAll( ConcurrentMap<String, String> map);
    void clear();
}