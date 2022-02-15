package io.github.yemyatthu1990.apm;

public class AgentConfiguration {

    private String collectorHost = "127.0.0.1";
    private int collectorPorts = 8200;
    private boolean collectorTLS = false;
    private boolean enableCrashMonitoring = true;
    private boolean enableANRReporting = true;
    private String secretToken = null;
    private String endpoint = "http://10.228.213.101:9411/api/v2/spans";

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getCollectorHost() {
        return collectorHost;
    }

    public void setCollectorHost(String collectorHost) {
        this.collectorHost = collectorHost;
    }

    public int getCollectorPorts() {
        return collectorPorts;
    }

    public void setCollectorPorts(int collectorPorts) {
        this.collectorPorts = collectorPorts;
    }

    public boolean isCollectorTLS() {
        return collectorTLS;
    }

    public void setCollectorTLS(boolean collectorTLS) {
        this.collectorTLS = collectorTLS;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    public boolean isEnableCrashMonitoring() {
        return enableCrashMonitoring;
    }

    public void setEnableCrashMonitoring(boolean enableCrashMonitoring) {
        this.enableCrashMonitoring = enableCrashMonitoring;
    }

    public boolean isEnableANRReporting() {
        return enableANRReporting;
    }

    public void setEnableANRReporting(boolean enableANRReporting) {
        this.enableANRReporting = enableANRReporting;
    }
}
