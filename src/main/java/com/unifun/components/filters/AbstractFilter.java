package com.unifun.components.filters;

import com.unifun.components.response.AnalyzeResponse;

import java.util.List;

public abstract class AbstractFilter<T> implements Filter<T>{
    public abstract AnalyzeResponse<T> filter(String text);

    public String buildErrorText(String data, String ...patterns) {
        List<String> lines = data.lines().toList();
        StringBuilder builder = new StringBuilder();
        for (String row : lines) {
            for (String pattern : patterns) {
                if (row.toLowerCase().contains(pattern)) {
                    builder.append(row).append("\n");
                }
            }
        }
        return builder.toString();
    }
}
