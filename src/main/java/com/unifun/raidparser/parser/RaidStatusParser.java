package com.unifun.raidparser.parser;

import com.unifun.raidparser.core.analyzer.Analyzer;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RaidStatusParser<T extends Status> {
    public Map<String, AnalyzeResponse<T>> getParsedData(Map<String, String> serversData, Analyzer<T> analyzer) {
        Map<String, AnalyzeResponse<T>> serversStatus = new HashMap<>();

        for (Map.Entry<String, String> entry : serversData.entrySet()) {
            AnalyzeResponse<T> analyzeResponse = analyzer.analyze(entry.getValue());
            serversStatus.put(entry.getKey(), analyzeResponse);
        }
        return serversStatus;
    }
}
