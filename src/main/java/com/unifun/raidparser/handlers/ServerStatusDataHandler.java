package com.unifun.raidparser.handlers;

import com.unifun.raidparser.builder.ServerStatusBuilder;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServerStatusDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerStatusDataHandler.class);
    private final ConcurrentHashMap<String, ServerStatus> serverStatuses = new ConcurrentHashMap<>();;

    public void updateAll(List<ServerStatus> serverStatuses) {
        serverStatuses.forEach(this::updateStatus);
    }

    public void updateStatus(ServerStatus newServerStatus) {
        String serverName = newServerStatus.serverName();
        LOGGER.debug("Updating status for server: {}", serverName);

        serverStatuses.compute(newServerStatus.serverName(), (key, currentServerStatus) -> {
            ServerStatusBuilder builder = new ServerStatusBuilder()
                    .serverName(newServerStatus.serverName());

            if (currentServerStatus != null) {
                currentServerStatus.healthStatusMap().forEach(builder::addHealthStatus);
            }

            newServerStatus.healthStatusMap().forEach((type, newResponse) -> {
                AnalyzeResponse<? extends Status> currentResponse = (currentServerStatus != null)
                        ? currentServerStatus.healthStatusMap().get(type)
                        : null;
                if (currentResponse == null) {
                    builder.addHealthStatus(type, newResponse);
                } else if (newResponse.getStatus().getPriority() < currentResponse.getStatus().getPriority()) {
                    LOGGER.info("Server '{}' for [{}] status changed from '{}' to '{}' due to higher priority",
                            serverName, type, currentResponse.getStatus(), newResponse.getStatus());
                    builder.addHealthStatus(type, newResponse);
                }
            });

            return builder.build();
        });

    }

    public void updateStatus(String serverName, HealthType type, AnalyzeResponse<Status> newResponse) {
        LOGGER.debug("Updating status for server: {}, component: {}, new status: {}", serverName, type, newResponse.getStatus());

        ServerStatusBuilder builder = new ServerStatusBuilder().serverName(serverName);
        serverStatuses.compute(serverName, (key, currentServerStatus) -> {
            if (currentServerStatus == null) {
                return builder.addHealthStatus(type, newResponse).build();
            }

            currentServerStatus.healthStatusMap().forEach(builder::addHealthStatus);
            AnalyzeResponse<? extends Status> currentResponse = currentServerStatus.healthStatusMap().get(type);

            if (currentResponse == null) {
                builder.addHealthStatus(type, newResponse);
            } else if (newResponse.getStatus().getPriority() < currentResponse.getStatus().getPriority()) {
                LOGGER.info("Server '{}' [{}] status changed from '{}' to '{}' due to higher priority",
                        serverName, type, currentResponse.getStatus(), newResponse.getStatus());
                builder.addHealthStatus(type, newResponse);
            }

            return builder.build();
        });
    }

    public ServerStatus get(String serverName) {
        return serverStatuses.get(serverName);
    }

    public List<ServerStatus> getAll() {
        return List.copyOf(serverStatuses.values());
    }

    public void clear() {
        LOGGER.debug("Clear server status cache");
        serverStatuses.clear();
    }
}
