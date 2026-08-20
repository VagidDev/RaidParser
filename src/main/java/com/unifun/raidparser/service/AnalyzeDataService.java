package com.unifun.raidparser.service;

import com.unifun.raidparser.builder.ServerStatusBuilder;
import com.unifun.raidparser.core.analyzer.Analyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerStatus;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyzeDataService {
    private static final Logger LOGGER = LogManager.getLogger(AnalyzeDataService.class);

    private final List<Analyzer<? extends Status>> analyzerList;

    public ServerStatus analyze(ServerData serverData) {
        if (serverData == null || CollectionUtils.isEmpty(serverData.rawDataByComponent())) {
            LOGGER.warn("Server data is empty or null, skipping analysis");
            return null;
        }

        ServerStatusBuilder serverStatusBuilder = new ServerStatusBuilder().serverName(serverData.serverName());

        for (Map.Entry<HealthType, String> entry : serverData.rawDataByComponent().entrySet()) {
            HealthType type = entry.getKey();
            String rawData = entry.getValue();

            AnalyzeResponse<? extends Status> response = runAnalysis(type, rawData);
            serverStatusBuilder.addHealthStatus(type, response);
        }

        return serverStatusBuilder.build();
    }

    private AnalyzeResponse<? extends Status> runAnalysis(HealthType healthType, String rawData){
        for (Analyzer<? extends Status> analyzer : analyzerList) {
            if (analyzer.getSupportedType() == healthType && analyzer.isSupportedRawData(rawData)) {
                return analyzer.analyze(rawData);
            }
        }
        LOGGER.warn("No suitable analyzer found for type {} with data: {}", healthType, rawData);
        return generateDefaultStatus(healthType);
    }

    private AnalyzeResponse<? extends Status> generateDefaultStatus(HealthType healthType) {
        return analyzerList.stream()
                .filter(analyzer -> analyzer.getSupportedType() == healthType)
                .findFirst()
                .map(analyzer -> new AnalyzeResponse<>(analyzer.getUnknownStatus(), "Unknown " + healthType + " data"))
                .orElseThrow(() -> new IllegalArgumentException("No analyzers registered for type: " + healthType));
    }

}
