package com.unifun.components;

import com.unifun.components.response.AnalyzeResponse;

public interface Analyzer<T> {
    AnalyzeResponse<T> analyze(String data);
}
