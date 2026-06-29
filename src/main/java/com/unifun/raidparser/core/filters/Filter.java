package com.unifun.raidparser.core.filters;

import com.unifun.raidparser.core.response.AnalyzeResponse;

public interface Filter<T> {
    AnalyzeResponse<T> filter(String text);
    String buildErrorText(String data, String ...patterns);
}
