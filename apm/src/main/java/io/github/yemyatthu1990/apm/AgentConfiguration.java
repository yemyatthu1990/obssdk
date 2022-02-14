package io.github.yemyatthu1990.apm;

public class AgentConfiguration {

    private String collectorHost = "127.0.0.1";
    private int collectorPorts = 8200;
    private boolean collectorTLS = false;
    private String secretToken = null;

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

}
