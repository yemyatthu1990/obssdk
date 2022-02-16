package io.github.yemyatthu1990.apm.collector;

import java.util.Locale;
import java.util.Map;

public class RuntimeAttributesCollector extends MetricsCollector {
    private final String runtimeCPUUsageKey = "system.cpu.usage";
    private final String runtimeMemoryTotalKey = "system.memory.total";
    private final String runtimeMemoryFreeKey = "system.memory.free";
    private final String runtimeMemoryUsageKey = "system.memory.usage";
    public RuntimeAttributesCollector() {

    }
    public Map<String, String> getRuntimeAttributes() {
        this.put(runtimeCPUUsageKey, String.format(Locale.ROOT, "%.2f %%",CpuInfo.getCpuUsageFromFreq()));
        this.put(runtimeMemoryFreeKey, String.format(Locale.ROOT, "%.2f GB",MemoryInfo.getFreeRam()));
        this.put(runtimeMemoryTotalKey, String.format(Locale.ROOT, "%.2f GB",MemoryInfo.getTotalRam()));
        this.put(runtimeMemoryUsageKey, String.format(Locale.ROOT, "%.2f %%", MemoryInfo.getUsageRam()));
        return this.map();
    }
}
