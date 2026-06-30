package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.component.ComponentType;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public interface Analyzer<T> {
    ComponentType getSupportedType();
    AnalyzeResponse<T> analyze(String rawData);
}
