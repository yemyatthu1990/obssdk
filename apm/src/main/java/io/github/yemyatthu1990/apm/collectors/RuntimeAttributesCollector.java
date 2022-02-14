package io.github.yemyatthu1990.apm.collectors;

import java.util.Map;

public class RuntimeAttributesCollector extends MetricsCollector{
    private final String runtimeCPUUsageKey = "runtime.cpu_usage";
    public Map<String, String> getRuntimeAttributes() {
        this.put(runtimeCPUUsageKey, String.valueOf(CpuInfo.getCpuUsageFromFreq()));
        return this.map();
    }
}
