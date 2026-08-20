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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyzeDataService {
    private static final Logger LOGGER = LogManager.getLogger(AnalyzeDataService.class);

    private final List<Analyzer<? extends Status>> analyzerList;

    /**
     * Всегда возвращает статус: null уходил в общий список и падал NPE
     * уже в кэше статусов и в сортировке. Сервер без данных попадает
     * в отчёт с UNKNOWN по всем известным типам.
     */
    public ServerStatus analyze(ServerData serverData) {
        if (serverData == null || !StringUtils.hasText(serverData.serverName())) {
            LOGGER.warn("Server data is null or has no server name, skipping analysis");
            return null;
        }

        if (CollectionUtils.isEmpty(serverData.rawDataByComponent())) {
            LOGGER.warn("Server `{}` has no raw data, reporting all components as unknown", serverData.serverName());
            return unknownStatus(serverData.serverName());
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

    private ServerStatus unknownStatus(String serverName) {
        ServerStatusBuilder builder = new ServerStatusBuilder().serverName(serverName);
        analyzerList.stream()
                .map(Analyzer::getSupportedType)
                .distinct()
                .forEach(type -> builder.addHealthStatus(type, generateDefaultStatus(type)));
        return builder.build();
    }

    private AnalyzeResponse<? extends Status> generateDefaultStatus(HealthType healthType) {
        return analyzerList.stream()
                .filter(analyzer -> analyzer.getSupportedType() == healthType)
                .findFirst()
                .map(analyzer -> new AnalyzeResponse<>(analyzer.getUnknownStatus(), "Unknown " + healthType + " data"))
                .orElseThrow(() -> new IllegalArgumentException("No analyzers registered for type: " + healthType));
    }

}
