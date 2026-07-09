package com.unifun.raidparser.dto;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;

import java.util.concurrent.ConcurrentHashMap;

public record ServerStatus(
        String serverName,
        ConcurrentHashMap<HealthType, AnalyzeResponse<? extends Status>> healthStatusMap
){

    public String getPrettyFormat() {
        return "Not implemented yet";
//                "Server Name: " + serverName
//                + " -> " + analyzeResponse.getStatus() + " -> " + analyzeResponse().getErrorText();
    }
}
