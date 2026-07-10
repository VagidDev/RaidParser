package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;

import java.util.List;

public abstract class AbstractAnalyzer<T extends Status> implements Analyzer<T> {
    protected abstract List<Filter<T>> getFilters();

    public AnalyzeResponse<T> analyze(String data) {
        for (Filter<T> filter : getFilters()) {
            if (filter.filter(data)) {
                return filter.getFilterResponse(data);
            }
        }

        return new AnalyzeResponse<>(
                getUnknownStatus(),
                "Cannot analyze received data -> " + data
        );
    }


}
