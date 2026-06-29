package com.unifun.raidparser.service;

import com.unifun.raidparser.handlers.HostOverviewParsedDataHandler;
import com.unifun.raidparser.dto.HostInformation;
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

    public List<HostInformation> getServers() {
        return hostOverviewParsedDataHandler.getServerData();
    }

    public List<HostInformation> getActualServers() {
        return hostOverviewParsedDataHandler.getActualServerData();
    }

    public List<HostInformation> getVirtualServers() {
        return hostOverviewParsedDataHandler.getServerData().stream()
                .filter(server -> server.getServerType().toLowerCase()
                        .matches(".*(xen|vmware|exoscale|virtual).*"))
                .toList();
    }

    public List<HostInformation> getPhysicalServers() {
        List<HostInformation> physicalServers = new ArrayList<>(hostOverviewParsedDataHandler.getServerData());
        physicalServers.removeAll(getVirtualServers());
        return physicalServers;
    }

    public List<HostInformation> getPhysicalServersWithCorrectPort() {
        return getPhysicalServers().stream()
                .filter(server -> server.getPort() != -1)
                .toList();
    }

    public List<HostInformation> getPhysicalServersWithCorrectPortByName(String serverName) {
        return getPhysicalServersWithCorrectPort().stream()
                .filter(serverInfo -> serverInfo.getName().contains(serverName))
                .toList();
    }

    public HostInformation getPhysicalServerWithCorrectPortByName(String serverName) {
        return getPhysicalServersWithCorrectPort().stream()
                .filter(serverInfo -> serverInfo.getName().contains(serverName))
                .findFirst()
                .orElse(null);
    }

    public List<HostInformation> getServersByName(String serverName) {
        return hostOverviewParsedDataHandler.getServerData().stream()
                .filter(server -> server.getName().contains(serverName))
                .toList();
    }

    public HostInformation getServerByName(String serverName) {
        return hostOverviewParsedDataHandler.getServerData().stream()
                .filter(server -> server.getName().contains(serverName))
                .findFirst()
                .orElse(null);
    }
}
