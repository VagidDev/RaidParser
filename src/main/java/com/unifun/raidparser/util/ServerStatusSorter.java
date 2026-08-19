package com.unifun.raidparser.util;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ServerStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ServerStatusSorter {
    public List<ServerStatus> sortByHealthStatus(List<ServerStatus> serversStatuses, HealthType healthType) {
        List<ServerStatus> sortedServersStatuses = new ArrayList<>(List.copyOf(serversStatuses));
        sortedServersStatuses.sort(Comparator.comparingInt(serverStatus -> {
                        if (serverStatus.healthStatusMap().get(healthType) == null)
                            return 1;
                        return serverStatus.healthStatusMap().get(healthType).getStatus().getPriority();
        }));
        return sortedServersStatuses;
    }
}
