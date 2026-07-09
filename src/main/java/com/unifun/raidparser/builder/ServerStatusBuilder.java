package com.unifun.raidparser.builder;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerStatus;

import java.util.concurrent.ConcurrentHashMap;

public class ServerStatusBuilder {
    private String serverName;
    private final ConcurrentHashMap<HealthType, AnalyzeResponse<? extends Status>> healthStatus = new ConcurrentHashMap<>();

    public ServerStatusBuilder serverName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public ServerStatusBuilder addHealthStatus(HealthType type, AnalyzeResponse<? extends Status> response) {
        if (type != null && response != null) {
            this.healthStatus.put(type, response);
        }
        return this;
    }

    public ServerStatusBuilder healthStatusMap(ConcurrentHashMap<HealthType, AnalyzeResponse<Status>> map) {
        if (map != null) {
            this.healthStatus.putAll(map);
        }
        return this;
    }

    public ServerStatus build() {
        if (serverName == null)
            throw new IllegalArgumentException("Server name is null");

        return new ServerStatus(this.serverName, new ConcurrentHashMap<>(healthStatus));
    }
}
