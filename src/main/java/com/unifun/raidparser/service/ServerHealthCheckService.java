package com.unifun.raidparser.service;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerTask;
import com.unifun.raidparser.handlers.ServersToCheckConfigFileDataHandler;
import com.unifun.raidparser.util.RemoteCommandExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerHealthCheckService {
    private static final Logger LOGGER = LogManager.getLogger(ServerHealthCheckService.class);

    private final ServersToCheckConfigFileDataHandler serversToCheckConfigFileDataHandler;
    private final RemoteCommandExecutor remoteCommandExecutor;
    private final ServersToCheckConfig serversToCheckConfig;
    private final HostOverviewService hostOverviewService;

    public List<ServerData> checkServers() {
        List<ServerTask> serverTasks = serversToCheckConfigFileDataHandler.getServerTasks();
        if (serverTasks == null) {
            LOGGER.warn("No tasks to check!");
            return List.of();
        }
        Map<ServerTask, HostInformation> hostsToCheck = getHostsToCheck(serverTasks);
        return hostsToCheck.entrySet().stream().
                map(entry -> checkServer(entry.getKey(), entry.getValue()))
                .toList();
    }

    public Map<ServerTask, HostInformation> getHostsToCheck(List<ServerTask> serverTasks) {
        return serverTasks.stream()
                .map(serverTask -> new AbstractMap.SimpleEntry<>(
                        serverTask,
                        hostOverviewService.getPhysicalServerWithCorrectPortByName(serverTask.getHostName())
                ))
                .filter(entry -> {
                    if (entry.getValue() == null) {
                        LOGGER.warn("Cannot check the server due to null reference received! Server task -> {}, Host information -> {}", entry.getKey(), entry.getValue());
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private ServerData checkServer(ServerTask serverTask, HostInformation hostInformation){
        String commandOutput = "";
        if (hostInformation.getConnectionType().equalsIgnoreCase("proxy"))
            commandOutput = remoteCommandExecutor.execute(
                    serversToCheckConfig.getProxyServerIp(),
                    hostInformation.getPort(),
                    serverTask.getCommandToExecute()
            );
        else
            commandOutput = remoteCommandExecutor.execute(
                    hostInformation.getIp(),
                    22,
                    serverTask.getCommandToExecute()
            );
        // I made this instead of simple editing of existing object so it will be more logically and easier to understand
        return new ServerData(
                serverTask.getHostName(),
                commandOutput
        );
    }



}
