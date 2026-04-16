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
        return null;
    }

    public List<HostInformation> getHostsToCheck(List<ServerTask> serverTasks) {
        List<String> nameOfHosts = serverTasks.stream().map(ServerTask::getHostName).toList();
        List<HostInformation> hostsFromHostOverview = hostOverviewService.getPhysicalServersWithCorrectPort();

        return hostsFromHostOverview.stream().filter(nameOfHosts::contains).toList();
    }


}
