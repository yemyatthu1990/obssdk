package io.github.yemyatthu1990.apm;

import java.util.Map;

class RuntimeAttributesCollector extends MetricsCollector{
    private final String runtimeCPUUsageKey = "system.cpu.usage";
    private final String runtimeMemoryTotalKey = "system.memory.total";
    private final String runtimeMemoryFreeKey = "system.memory.free";
    private final String runtimeMemoryUsageKey = "system.memory.usage";
    public RuntimeAttributesCollector() {

    }
    Map<String, String> getRuntimeAttributes() {
        this.put(runtimeCPUUsageKey, String.valueOf(CpuInfo.getCpuUsageFromFreq()));
        this.put(runtimeMemoryFreeKey, String.valueOf(MemoryInfo.getFreeRam()));
        this.put(runtimeMemoryTotalKey, String.valueOf(MemoryInfo.getTotalRam()));
        this.put(runtimeMemoryUsageKey, String.format("%.2f",MemoryInfo.getUsageRam()));
        return this.map();
    }
}
