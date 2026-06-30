package com.unifun.raidparser.parser;

import com.unifun.raidparser.core.analyzer.Analyzer;
import com.unifun.raidparser.core.component.ComponentType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RaidStatusParser<T extends Status> {
    private static final Logger LOGGER = LogManager.getLogger(RaidStatusParser.class);

    public List<ServerStatus<T>> getParsedData(List<ServerData> serversData, Analyzer<T> analyzer) {
        ComponentType supportedType = analyzer.getSupportedType();
        LOGGER.info("Got `{}` supported type", supportedType);
        List<ServerStatus<T>> serversStatus = new ArrayList<>();

        for (ServerData serverData : serversData) {
            String rawDta = serverData.getRawData(supportedType);
            LOGGER.info("Got raw data for analyzing -> {}", rawDta);
            AnalyzeResponse<T> analyzeResponse = analyzer.analyze(rawDta);
            serversStatus.add(new ServerStatus<>(serverData.serverName(), analyzeResponse));
        }
        return serversStatus;
    }
}
