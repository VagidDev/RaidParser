package com.unifun.raidparser.core.filters;

import com.unifun.raidparser.core.response.AnalyzeResponse;

public interface Filter<T> {
    boolean filter(String text);
    AnalyzeResponse<T> getFilterResponse(String text);
    String buildErrorText(String data, String ...patterns);
}
