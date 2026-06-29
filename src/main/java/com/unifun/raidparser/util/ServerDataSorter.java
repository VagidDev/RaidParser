package com.unifun.raidparser.util;

import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.dto.ServerStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServerDataSorter<T extends Status> {
    public List<ServerStatus<T>> sortByStatus(List<ServerStatus<T>> serversStatuses) {
        List<ServerStatus<T>> sortedServersStatuses = new ArrayList<>(List.copyOf(serversStatuses));
        sortedServersStatuses.sort(Comparator.comparingInt(serverStatus -> serverStatus.analyzeResponse().getStatus().getPriority()));
        return sortedServersStatuses;
    }
}
