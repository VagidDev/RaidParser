package com.unifun.components.filters;

import com.unifun.components.response.AnalyzeResponse;

public interface Filter<T> {
    AnalyzeResponse<T> filter(String text);
    String buildErrorText(String data, String ...patterns);
}
