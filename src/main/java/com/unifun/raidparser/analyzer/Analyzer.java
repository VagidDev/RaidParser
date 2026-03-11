package com.unifun.raidparser.analyzer;

import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

public interface Analyzer<T> {
    AnalyzeResponse<T> analyze(String data);
}
