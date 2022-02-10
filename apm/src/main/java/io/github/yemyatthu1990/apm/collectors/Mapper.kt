package io.github.yemyatthu1990.apm.collectors

import java.util.concurrent.ConcurrentMap

interface Mapper {
    fun size(): Int
    val isEmpty: Boolean
    fun hasKey(key: String): Boolean
    fun getValue(key: String): String?
    fun map(): ConcurrentMap<String, String>?
    val keySet: Set<String>?
    fun valueSet(): Collection<String>?
    fun put(key: String, value: String)
    fun putAll(map: ConcurrentMap<String, String>?)
    fun clear()
}