package com.unifun.components.filters;

import com.unifun.components.response.AnalyzeResponse;

public abstract class EmptyFilter<T> extends AbstractFilter<T> {
    @Override
    public AnalyzeResponse<T> filter(String text) {
        if (text.isBlank()) {
            return getInvalidResponse();
        }
        return getValidResponse();
    }

    protected abstract AnalyzeResponse<T> getValidResponse();
    protected abstract AnalyzeResponse<T> getInvalidResponse();
}
