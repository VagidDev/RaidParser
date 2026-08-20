package com.unifun.raidparser.mapper;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.dto.ServerStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ExportDataMapperTest {
    private final ExportDataMapper mapper = new ExportDataMapper();

    private ServerStatus serverStatus(String name, HealthType type, DriverStatus status) {
        ConcurrentHashMap<HealthType, AnalyzeResponse<? extends Status>> map = new ConcurrentHashMap<>();
        if (type != null) {
            map.put(type, new AnalyzeResponse<>(status, "error text"));
        }
        return new ServerStatus(name, map);
    }

    @Test
    void map_mapsStatusAndErrorText() {
        List<ReportServerData> result = mapper.map(
                List.of(serverStatus("host-01", HealthType.DRIVE_HEALTH, DriverStatus.FAILED)),
                HealthType.DRIVE_HEALTH
        );

        assertEquals(1, result.size());
        assertEquals("host-01", result.get(0).serverName());
        assertEquals(DriverStatus.FAILED.getName(), result.get(0).healthStatus());
        assertEquals("error text", result.get(0).errorText());
    }

    @Test
    void map_serverWithoutRequestedHealthType_doesNotThrow() {
        // Регрессия: отсутствующий тип давал NPE и ронял весь экспорт.
        List<ReportServerData> result = mapper.map(
                List.of(serverStatus("host-01", null, null)),
                HealthType.PSU_HEALTH
        );

        assertEquals(1, result.size());
        assertEquals("NO DATA", result.get(0).healthStatus());
        assertEquals("", result.get(0).errorText());
    }

    @Test
    void map_skipsNullServerStatuses() {
        List<ReportServerData> result = mapper.map(
                Arrays.asList(serverStatus("host-01", HealthType.DRIVE_HEALTH, DriverStatus.OK), null),
                HealthType.DRIVE_HEALTH
        );

        assertEquals(1, result.size());
    }

    @Test
    void map_nullList_returnsEmptyList() {
        assertTrue(mapper.map(null, HealthType.DRIVE_HEALTH).isEmpty());
    }
}
