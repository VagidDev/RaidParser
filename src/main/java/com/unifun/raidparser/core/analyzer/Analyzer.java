package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public interface Analyzer<T> {
    HealthType getSupportedType();
    AnalyzeResponse<T> analyze(String rawData);
}
