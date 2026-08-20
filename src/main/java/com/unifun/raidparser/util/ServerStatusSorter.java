package com.unifun.raidparser.util;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ServerStatusSorter {
    public List<ServerStatus> sortByHealthStatus(List<ServerStatus> serversStatuses, HealthType healthType) {
        if (serversStatuses == null) {
            return List.of();
        }

        List<ServerStatus> sortedServersStatuses = new ArrayList<>(serversStatuses.size());
        serversStatuses.stream().filter(Objects::nonNull).forEach(sortedServersStatuses::add);

        sortedServersStatuses.sort(Comparator.comparingInt(serverStatus -> priorityOf(serverStatus, healthType)));
        return sortedServersStatuses;
    }

    /**
     * Отсутствие статуса — это «нет данных», а не «почти критично»:
     * в шкале Status меньший приоритет означает более серьёзную проблему,
     * поэтому такие серверы уходят в конец, как и UNKNOWN.
     */
    private int priorityOf(ServerStatus serverStatus, HealthType healthType) {
        AnalyzeResponse<? extends Status> response = serverStatus.healthStatusMap().get(healthType);
        if (response == null || response.getStatus() == null) {
            return Integer.MAX_VALUE;
        }
        return response.getStatus().getPriority();
    }
}
