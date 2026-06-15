package com.unifun.raidparser.dto;

import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public record ServerStatus<T extends Status>(
        String serverName,
        AnalyzeResponse<T> analyzeResponse
){}
