package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

import java.util.List;

public abstract class AbstractAnalyzer<T> implements Analyzer<T> {
    protected abstract List<Filter<T>> getFilters();
    protected abstract T getSuccessfulStatus();

    public AnalyzeResponse<T> analyze(String data) {
        AnalyzeResponse<T> response = null;

        for (Filter<T> filter : getFilters()) {
            response = filter.filter(data);
            if (response.getStatus() != getSuccessfulStatus()) {
                return response;
            }
        }

        return response;
    }


}
