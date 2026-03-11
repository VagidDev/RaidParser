package com.unifun.raidparser.service;

import com.unifun.raidparser.parser.HostOverviewParser;
import com.unifun.raidparser.dto.ServerInfoDTO;

import java.util.ArrayList;
import java.util.List;

public class HostOverviewService {
    private static final HostOverviewParser PARSER = new HostOverviewParser();

    public static List<ServerInfoDTO> getServers() {
        return PARSER.getServers();
    }

    public static List<ServerInfoDTO> getActualServers() {
        return PARSER.getActualServers();
    }

    public static List<ServerInfoDTO> getVirtualServers() {
        return PARSER.getServers().stream()
                .filter(server -> server.getServerType().toLowerCase()
                        .matches(".*(xen|vmware|exoscale|virtual).*"))
                .toList();
    }

    public static List<ServerInfoDTO> getPhysicalServers() {
        List<ServerInfoDTO> physicalServers = new ArrayList<>(PARSER.getServers());
        physicalServers.removeAll(getVirtualServers());
        return physicalServers;
    }

    public static List<ServerInfoDTO> getPhysicalServersWithCorrectPort() {
        return getPhysicalServers().stream()
                .filter(server -> server.getPort() != -1)
                .toList();
    }

    public static List<ServerInfoDTO> getPhysicalServersWithCorrectPortByName(String serverName) {
        return getPhysicalServersWithCorrectPort().stream()
                .filter(serverInfo -> serverInfo.getName().contains(serverName))
                .toList();
    }

    public static ServerInfoDTO getPhysicalServerWithCorrectPortByName(String serverName) {
        return getPhysicalServersWithCorrectPort().stream()
                .filter(serverInfo -> serverInfo.getName().contains(serverName))
                .findFirst()
                .orElse(null);
    }

    public static List<ServerInfoDTO> getServersByName(String serverName) {
        return PARSER.getServers().stream()
                .filter(server -> server.getName().contains(serverName))
                .toList();
    }

    public static ServerInfoDTO getServerByName(String serverName) {
        return PARSER.getServers().stream()
                .filter(server -> server.getName().contains(serverName))
                .findFirst()
                .orElse(null);
    }
}
