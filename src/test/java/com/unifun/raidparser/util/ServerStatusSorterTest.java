package com.unifun.raidparser.util;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ServerStatusSorterTest {
    private final ServerStatusSorter sorter = new ServerStatusSorter();

    private ServerStatus withStatus(String name, DriverStatus status) {
        ConcurrentHashMap<HealthType, AnalyzeResponse<? extends Status>> map = new ConcurrentHashMap<>();
        if (status != null) {
            map.put(HealthType.DRIVE_HEALTH, new AnalyzeResponse<>(status, ""));
        }
        return new ServerStatus(name, map);
    }

    @Test
    void sortByHealthStatus_mostSevereFirst() {
        List<ServerStatus> sorted = sorter.sortByHealthStatus(
                List.of(
                        withStatus("ok", DriverStatus.OK),
                        withStatus("degraded", DriverStatus.DEGRADED),
                        withStatus("predictive", DriverStatus.PREDICTIVE_FAILURE)
                ),
                HealthType.DRIVE_HEALTH
        );

        assertEquals(List.of("degraded", "predictive", "ok"),
                sorted.stream().map(ServerStatus::serverName).toList());
    }

    @Test
    void sortByHealthStatus_serverWithoutStatusGoesLast() {
        // Регрессия: отсутствие статуса получало приоритет 1 и вставало
        // выше реальных проблем вроде PREDICTIVE_FAILURE.
        List<ServerStatus> sorted = sorter.sortByHealthStatus(
                List.of(
                        withStatus("no-data", null),
                        withStatus("ok", DriverStatus.OK),
                        withStatus("predictive", DriverStatus.PREDICTIVE_FAILURE)
                ),
                HealthType.DRIVE_HEALTH
        );

        assertEquals("no-data", sorted.get(sorted.size() - 1).serverName());
        assertEquals("predictive", sorted.get(0).serverName());
    }

    @Test
    void sortByHealthStatus_ignoresNullElements() {
        List<ServerStatus> sorted = sorter.sortByHealthStatus(
                Arrays.asList(withStatus("ok", DriverStatus.OK), null),
                HealthType.DRIVE_HEALTH
        );

        assertEquals(1, sorted.size());
    }

    @Test
    void sortByHealthStatus_nullList_returnsEmptyList() {
        assertTrue(sorter.sortByHealthStatus(null, HealthType.DRIVE_HEALTH).isEmpty());
    }
}
