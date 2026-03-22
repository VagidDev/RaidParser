package com.unifun.raidparser.util;

import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServerDataSorter<T extends Status> {
    public List<Map.Entry<String, AnalyzeResponse<T>>> sortByStatus(Map<String, AnalyzeResponse<T>> serversStatus) {
        // Сортировка по приоритету
        List<Map.Entry<String, AnalyzeResponse<T>>> sortedServersStatus = new ArrayList<>(serversStatus.entrySet());
        sortedServersStatus.sort(Comparator.comparingInt(entry -> entry.getValue().getStatus().getPriority()));

        return sortedServersStatus;
    }

}
