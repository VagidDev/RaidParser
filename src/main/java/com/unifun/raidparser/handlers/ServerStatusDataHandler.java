package com.unifun.raidparser.handlers;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServerStatusDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerStatusDataHandler.class);
    private final ConcurrentHashMap<String, ServerStatus> serverStatuses = new ConcurrentHashMap<>();;

    public void updateStatus(String serverName, HealthType type, AnalyzeResponse<Status> newResponse) {
        LOGGER.debug("Updating status for server: {}, component: {}, new status: {}", serverName, type, newResponse.getStatus());

        serverStatuses.compute(serverName, (key, currentServerStatus) -> {
            if (currentServerStatus == null) {
                ConcurrentHashMap<HealthType, AnalyzeResponse<? extends Status>> initialMap = new ConcurrentHashMap<>();
                initialMap.put(type, newResponse);
                return new ServerStatus(serverName, initialMap);
            }

            Map<HealthType, AnalyzeResponse<? extends Status>> healthMap = currentServerStatus.healthStatusMap();

            healthMap.compute(type, (componentKey, currentResponse) -> {
                if (currentResponse == null) {
                    return newResponse;
                }

                if (newResponse.getStatus().getPriority() < currentResponse.getStatus().getPriority()) {
                    LOGGER.info("Server '{}' [{}] status changed from '{}' to '{}' due to higher priority",
                            serverName, type, currentResponse.getStatus(), newResponse.getStatus());
                    return newResponse;
                }

                return currentResponse;
            });

            return currentServerStatus;
        });
    }

    public void add(ServerStatus serverStatus) {
        serverStatuses.put(serverStatus.serverName(), serverStatus);
    }

    public ServerStatus get(String serverName) {
        return serverStatuses.get(serverName);
    }

    public List<ServerStatus> getAll() {
        return List.copyOf(serverStatuses.values());
    }
}
