package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.response.AnalyzeResponse;

public interface Analyzer<T> {
    AnalyzeResponse<T> analyze(String data);
}
