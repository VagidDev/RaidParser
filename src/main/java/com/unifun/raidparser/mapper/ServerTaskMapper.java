package com.unifun.raidparser.mapper;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.HostCommand;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.dto.ServerTask;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
@RequiredArgsConstructor
public class ServerTaskMapper {
    private static final Logger LOGGER = LogManager.getLogger(ServerTaskMapper.class);

    private final ServersToCheckConfig serversToCheckConfig;

    public List<ServerTask> map(List<HostCommand> hostCommandList, List<HostInformation> hostInformationList) {
        if (CollectionUtils.isEmpty(hostCommandList) || CollectionUtils.isEmpty(hostInformationList)) {
            LOGGER.warn("Received empty list for mapping!");
            return List.of();
        }
        ConcurrentSkipListMap<String, ServerTask> map = new ConcurrentSkipListMap<>();

        hostCommandList.forEach(hostCommand -> {
            for (HostInformation hostInformation : hostInformationList) {
                if (hostCommand.getHost().equalsIgnoreCase(hostInformation.getName())) {
                    if (map.containsKey(hostCommand.getHost())) {
                        ServerTask serverTask = map.get(hostCommand.getHost());
                        serverTask
                                .healthCommand()
                                .putIfAbsent(hostCommand.getType(), hostCommand.getCommand());
                        LOGGER.debug(
                                "Add to host {} new health command: health type -> {} command -> {}; All health commands for this host -> {}",
                                serverTask.hostname(),
                                hostCommand.getType(),
                                hostCommand.getCommand(),
                                serverTask.healthCommand()
                        );
                    } else {
                        ServerTask serverTask = buildServerTask(hostCommand, hostInformation);
                        map.put(serverTask.hostname(), serverTask);
                    }
                }
            }
        });

        return map.values().stream().toList();
    }

    private ServerTask buildServerTask(HostCommand hostCommand, HostInformation hostInformation) {
        String ip = serversToCheckConfig.getProxyServerIp();
        int port = 22;
        ConcurrentSkipListMap<HealthType, String> healthTypeStringConcurrentSkipListMap = new ConcurrentSkipListMap<>();
        if (hostInformation.getConnectionType().equalsIgnoreCase("proxy")) {
            port = hostInformation.getPort();
        } else {
            ip = hostInformation.getIp();
        }
        healthTypeStringConcurrentSkipListMap.put(hostCommand.getType(), hostCommand.getCommand());

        ServerTask serverTask = new ServerTask(
                hostCommand.getHost(),
                ip,
                port,
                healthTypeStringConcurrentSkipListMap
        );

        LOGGER.debug(
                "Built serverTask with hostname {} ip {} and port {} -> Health Command -> {}",
                serverTask.hostname(),
                serverTask.ip(),
                serverTask.port(),
                serverTask.healthCommand()
        );

        return serverTask;
    }

}
