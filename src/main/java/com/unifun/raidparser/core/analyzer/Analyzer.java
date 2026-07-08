package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public interface Analyzer<T extends Status> {
    boolean isSupportedRawData(String text);
    HealthType getSupportedType();
    AnalyzeResponse<T> analyze(String rawData);
}
