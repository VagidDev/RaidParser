package com.unifun.raidparser.parser;

import com.unifun.raidparser.core.analyzer.Analyzer;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RaidStatusParser<T extends Status> {
    public List<ServerStatus<T>> getParsedData(List<ServerData> serversData, Analyzer<T> analyzer) {
        List<ServerStatus<T>> serversStatus = new ArrayList<>();

        for (ServerData serverData : serversData) {
            AnalyzeResponse<T> analyzeResponse = analyzer.analyze(serverData);
            serversStatus.add(new ServerStatus<>(serverData.serverName(), analyzeResponse));
        }
        return serversStatus;
    }
}
