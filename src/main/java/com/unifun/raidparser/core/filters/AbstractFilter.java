package com.unifun.raidparser.core.filters;

import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.util.TextSearcher;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFilter<T extends Status> implements Filter<T>{
    public String buildErrorText(String data, String ...patterns) {
        List<String> lines = data.lines().toList();
        List<String> matchedLines = new ArrayList<>();

        for (String row : lines) {
            for (String pattern : patterns) {
                if (TextSearcher.containsAll(row, pattern)) {
                    matchedLines.add(row);
                    break;
                }
            }
        }
        return String.join("\n", matchedLines);
    }
}
