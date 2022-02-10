package io.github.yemyatthu1990.apm.collectors

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

open class MetricsCollector : Mapper{
    private val cache: ConcurrentMap<String, String>
    init {
        cache = ConcurrentHashMap()
    }
    override fun size() = cache.size

    override val isEmpty = cache.isEmpty()
    override fun hasKey(key: String) = cache.containsKey(key)

    override fun getValue(key: String): String = cache.getValue(key)


    override fun map(): ConcurrentMap<String, String> = cache

    override val keySet: Set<String> = cache.keys

    override fun valueSet(): Collection<String> = cache.values
    override fun put(key: String, value: String) {
        cache[key] = value
    }

    override fun putAll(map: ConcurrentMap<String, String>?) {
        map?.let {
            cache.putAll(map)
        }
    }


    override fun clear() {
        cache.clear()
    }


}