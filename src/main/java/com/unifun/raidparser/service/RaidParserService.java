package com.unifun.raidparser.service;

import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.handlers.ServerStatusDataHandler;
import com.unifun.raidparser.handlers.ServerDataHandler;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RaidParserService {
    private static final Logger LOGGER = LogManager.getLogger(RaidParserService.class);

    private final ServerDataHandler serverDataHandler;
    private final ServerStatusDataHandler serverStatusDataHandler;
    private final ServerHealthCheckService serverHealthCheckService;
    private final AnalyzeDataService analyzeDataService;

    public List<ServerStatus> analyzeStatusFromReportFile(Path reportFile) {
        List<ServerData> serverDataList = serverDataHandler.getServerData(reportFile);
        return analyzeData(serverDataList);
    }

    public List<ServerStatus> analyzeStatusFromHosts() {
        List<ServerData> serverDataList = serverHealthCheckService.checkServersParallel();
        return analyzeData(serverDataList);
    }

    private List<ServerStatus> analyzeData(List<ServerData> serverDataList) {
        List<ServerStatus> serverStatuses = serverDataList.stream()
                .map(analyzeDataService::analyze)
                .filter(Objects::nonNull)
                .toList();
        serverStatusDataHandler.updateAll(serverStatuses);
        return serverStatuses;
    }

    public List<ServerStatus> getCachedStatus() {
        return serverStatusDataHandler.getAll();
    }

    public void clearCache() {
        serverStatusDataHandler.clear();
    }

}