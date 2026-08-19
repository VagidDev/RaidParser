package com.unifun.raidparser.service;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.dto.HostCommand;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerTask;
import com.unifun.raidparser.handlers.ServersToCheckConfigFileDataHandler;
import com.unifun.raidparser.mapper.ServerTaskMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServerHealthCheckService {
    private static final Logger LOGGER = LogManager.getLogger(ServerHealthCheckService.class);

    private final ServersToCheckConfigFileDataHandler serversToCheckConfigFileDataHandler;
    private final HostExecutorService hostExecutorService;
    private final HostOverviewService hostOverviewService;
    private final ServerTaskMapper serverTaskMapper;

    public List<ServerData> checkServersParallel() {
        List<HostCommand> hostCommands = serversToCheckConfigFileDataHandler.getHostCommands();
        LOGGER.info("Got {} commands to execute", hostCommands.size());
        List<HostInformation> hostInformationList = hostOverviewService.getPhysicalServersWithCorrectPort();
        if (CollectionUtils.isEmpty(hostCommands) || CollectionUtils.isEmpty(hostInformationList)) {
            LOGGER.warn("Cannot check servers due to receiving empty collection!");
            return List.of();
        }
        List<ServerTask> serverTasks = serverTaskMapper.map(hostCommands, hostInformationList);
        LOGGER.info("Got {} servers for executing", serverTasks.size());
        List<ServerData> serverData = hostExecutorService.execute(serverTasks);
        LOGGER.info("Got {} servers data after executing", serverData.size());
        return serverData;
    }

}
