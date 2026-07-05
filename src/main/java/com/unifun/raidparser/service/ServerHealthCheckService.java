package com.unifun.raidparser.service;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.dto.HostCommand;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.dto.ServerData;
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
        List<HostCommand> hostCommands = serversToCheckConfigFileDataHandler.getHostCommands();
        if (hostCommands == null) {
            LOGGER.warn("No tasks to check!");
            return List.of();
        }
        Map<HostCommand, HostInformation> hostsToCheck = getHostsToCheck(hostCommands);
        return hostsToCheck.entrySet().stream().
                map(entry -> checkServer(entry.getKey(), entry.getValue()))
                .toList();
    }

    public Map<HostCommand, HostInformation> getHostsToCheck(List<HostCommand> hostCommands) {
        return hostCommands.stream()
                .map(serverTask -> new AbstractMap.SimpleEntry<>(
                        serverTask,
                        hostOverviewService.getPhysicalServerWithCorrectPortByName(serverTask.getHost())
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

    private ServerData checkServer(HostCommand hostCommand, HostInformation hostInformation){
        String commandOutput = "";
        if (hostInformation.getConnectionType().equalsIgnoreCase("proxy"))
            commandOutput = remoteCommandExecutor.execute(
                    serversToCheckConfig.getProxyServerIp(),
                    hostInformation.getPort(),
                    hostCommand.getCommand()
            );
        else
            commandOutput = remoteCommandExecutor.execute(
                    hostInformation.getIp(),
                    22,
                    hostCommand.getCommand()
            );
        LOGGER.debug("Output of the command {} is: {}", hostCommand.getCommand(), commandOutput);
        return new ServerData(
                hostCommand.getCommand(),
                Map.of(hostCommand.getType(), commandOutput)
        );
    }



}
