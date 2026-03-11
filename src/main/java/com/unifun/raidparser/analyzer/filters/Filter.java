package com.unifun.raidparser.analyzer.filters;

import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

public interface Filter<T> {
    AnalyzeResponse<T> filter(String text);
    String buildErrorText(String data, String ...patterns);
}
