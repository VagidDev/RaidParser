package com.unifun.parser;

import com.unifun.components.Analyzer;
import com.unifun.components.filters.Status;
import com.unifun.components.response.AnalyzeResponse;

import java.util.HashMap;
import java.util.Map;

public class RaidParser<T extends Status> {
    public Map<String, AnalyzeResponse<T>> getParsedData(Map<String, String> serversData, Analyzer<T> analyzer) {
        Map<String, AnalyzeResponse<T>> serversStatus = new HashMap<>();

        for (Map.Entry<String, String> entry : serversData.entrySet()) {
            AnalyzeResponse<T> analyzeResponse = analyzer.analyze(entry.getValue());
            serversStatus.put(entry.getKey(), analyzeResponse);
        }
        return serversStatus;
    }
}
