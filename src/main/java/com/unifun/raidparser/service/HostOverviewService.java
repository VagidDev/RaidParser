package com.unifun.raidparser.service;

import com.unifun.raidparser.handlers.HostOverviewParsedDataHandler;
import com.unifun.raidparser.dto.ServerInfo;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HostOverviewService {
    private static final Logger LOGGER = LogManager.getLogger(HostOverviewService.class);
    private final HostOverviewParsedDataHandler hostOverviewParsedDataHandler;

    public void actualizeServers() {
        hostOverviewParsedDataHandler.loadServers();
    }

    public void clearServerCache() {
        hostOverviewParsedDataHandler.clearCache();
    }

    public List<ServerInfo> getServers() {
        return hostOverviewParsedDataHandler.getServerData();
    }

    public List<ServerInfo> getActualServers() {
        return hostOverviewParsedDataHandler.getActualServerData();
    }

    public List<ServerInfo> getVirtualServers() {
        return hostOverviewParsedDataHandler.getServerData().stream()
                .filter(server -> server.getServerType().toLowerCase()
                        .matches(".*(xen|vmware|exoscale|virtual).*"))
                .toList();
    }

    public List<ServerInfo> getPhysicalServers() {
        List<ServerInfo> physicalServers = new ArrayList<>(hostOverviewParsedDataHandler.getServerData());
        physicalServers.removeAll(getVirtualServers());
        return physicalServers;
    }

    public List<ServerInfo> getPhysicalServersWithCorrectPort() {
        return getPhysicalServers().stream()
                .filter(server -> server.getPort() != -1)
                .toList();
    }

    public List<ServerInfo> getPhysicalServersWithCorrectPortByName(String serverName) {
        return getPhysicalServersWithCorrectPort().stream()
                .filter(serverInfo -> serverInfo.getName().contains(serverName))
                .toList();
    }

    public ServerInfo getPhysicalServerWithCorrectPortByName(String serverName) {
        return getPhysicalServersWithCorrectPort().stream()
                .filter(serverInfo -> serverInfo.getName().contains(serverName))
                .findFirst()
                .orElse(null);
    }

    public List<ServerInfo> getServersByName(String serverName) {
        return hostOverviewParsedDataHandler.getServerData().stream()
                .filter(server -> server.getName().contains(serverName))
                .toList();
    }

    public ServerInfo getServerByName(String serverName) {
        return hostOverviewParsedDataHandler.getServerData().stream()
                .filter(server -> server.getName().contains(serverName))
                .findFirst()
                .orElse(null);
    }
}
