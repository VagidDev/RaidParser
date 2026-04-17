package com.unifun.raidparser.service;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.dto.ServerTask;
import com.unifun.raidparser.handlers.ServersToCheckConfigFileDataHandler;
import com.unifun.raidparser.util.RemoteCommandExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

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

    public List<ServerTask> checkServers() {
        //TODO: need to be implemented
        List<ServerTask> serverTasks = serversToCheckConfigFileDataHandler.getServerTasks();
        Map<ServerTask, HostInformation> hostsToCheck = getHostsToCheck(serverTasks);
        //TODO: need to think how to change Map to List
        return null;
    }

    public Map<ServerTask, HostInformation> getHostsToCheck(List<ServerTask> serverTasks) {
        return serverTasks.stream().collect(Collectors
                .toMap(serverTask -> serverTask,
                        serverTask -> hostOverviewService.getPhysicalServerWithCorrectPortByName(serverTask.getHostName()))
        );
    }

    private ServerTask checkServer(ServerTask serverTask, HostInformation hostInformation){
        if (serverTask == null || hostInformation == null) {
            LOGGER.warn("Cannot check the server due to null reference received! Server task -> {}, Host information -> {}", serverTask, hostInformation);
            return new ServerTask();
        }

        String commandOutput = "";
        if (hostInformation.getServerType().equalsIgnoreCase("proxy"))
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
        return new ServerTask(
                serverTask.getHostName(),
                serverTask.getCommandToExecute(),
                commandOutput
        );
    }



}
