package com.unifun.raidparser.core.filters;

import com.unifun.raidparser.core.response.AnalyzeResponse;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFilter<T> implements Filter<T>{
    public abstract AnalyzeResponse<T> filter(String text);

    public String buildErrorText(String data, String ...patterns) {
        List<String> lines = data.lines().toList();
        List<String> matchedLines = new ArrayList<>();

        for (String row : lines) {
            for (String pattern : patterns) {
                if (row.toLowerCase().contains(pattern)) {
                    matchedLines.add(row);
                    break;
                }
            }
        }
        return String.join("\n", matchedLines);
    }
}
