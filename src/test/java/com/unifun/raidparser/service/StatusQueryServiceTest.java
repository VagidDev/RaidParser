package com.unifun.raidparser.service;

import com.unifun.raidparser.controllers.dto.ServerStatusResponse;
import com.unifun.raidparser.controllers.dto.StatusSummaryResponse;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.component.Severity;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.mapper.ApiStatusMapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatusQueryServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-21T10:00:00Z"), ZoneOffset.UTC);

    private final RaidParserService raidParserService = mock(RaidParserService.class);
    private final StatusQueryService statusQueryService =
            new StatusQueryService(raidParserService, new ApiStatusMapper(), CLOCK);

    private ServerStatus serverStatus(String name, DriverStatus drive, PowerSupplyStatus psu, BatteryStatus battery) {
        ConcurrentHashMap<HealthType, AnalyzeResponse<? extends Status>> map = new ConcurrentHashMap<>();
        if (drive != null) {
            map.put(HealthType.DRIVE_HEALTH, new AnalyzeResponse<>(drive, "drive details"));
        }
        if (psu != null) {
            map.put(HealthType.PSU_HEALTH, new AnalyzeResponse<>(psu, ""));
        }
        if (battery != null) {
            map.put(HealthType.BATTERY_HEALTH, new AnalyzeResponse<>(battery, ""));
        }
        return new ServerStatus(name, map);
    }

    private void givenCached(ServerStatus... statuses) {
        when(raidParserService.getCachedStatus()).thenReturn(List.of(statuses));
    }

    @Test
    void query_returnsWorstSeverityPerServer_andSortsCriticalFirst() {
        givenCached(
                serverStatus("healthy", DriverStatus.OK, PowerSupplyStatus.OK, BatteryStatus.OK),
                serverStatus("broken", DriverStatus.DEGRADED, PowerSupplyStatus.OK, BatteryStatus.OK),
                serverStatus("warned", DriverStatus.PREDICTIVE_FAILURE, PowerSupplyStatus.OK, BatteryStatus.OK)
        );

        List<ServerStatusResponse> result = statusQueryService.query(null, Set.of(), null);

        assertEquals(List.of("broken", "warned", "healthy"), result.stream().map(ServerStatusResponse::server).toList());
        assertEquals(Severity.CRITICAL, result.get(0).severity());
        assertEquals(Severity.OK, result.get(2).severity());
    }

    @Test
    void query_missingComponent_isReportedAsNoData() {
        givenCached(serverStatus("partial", DriverStatus.OK, null, null));

        ServerStatusResponse server = statusQueryService.query(null, Set.of(), null).get(0);

        assertEquals(Severity.NO_DATA, server.components().get(HealthType.PSU_HEALTH).severity());
        assertEquals("NO DATA", server.components().get(HealthType.PSU_HEALTH).status());
        // сервер без части данных не считается здоровым
        assertEquals(Severity.NO_DATA, server.severity());
    }

    @Test
    void query_filtersByComponent_andKeepsOnlyThatComponent() {
        givenCached(serverStatus("host-01", DriverStatus.OK, PowerSupplyStatus.FAILED, BatteryStatus.OK));

        ServerStatusResponse server = statusQueryService.query(HealthType.PSU_HEALTH, Set.of(), null).get(0);

        assertEquals(Set.of(HealthType.PSU_HEALTH), server.components().keySet());
        assertEquals(Severity.CRITICAL, server.severity());
    }

    @Test
    void query_filtersBySeverityAndName() {
        givenCached(
                serverStatus("db-01", DriverStatus.DEGRADED, PowerSupplyStatus.OK, BatteryStatus.OK),
                serverStatus("db-02", DriverStatus.OK, PowerSupplyStatus.OK, BatteryStatus.OK),
                serverStatus("web-01", DriverStatus.DEGRADED, PowerSupplyStatus.OK, BatteryStatus.OK)
        );

        assertEquals(
                List.of("db-01", "web-01"),
                statusQueryService.query(null, Set.of(Severity.CRITICAL), null).stream().map(ServerStatusResponse::server).toList());
        assertEquals(
                List.of("db-01"),
                statusQueryService.query(null, Set.of(Severity.CRITICAL), "DB").stream().map(ServerStatusResponse::server).toList());
    }

    @Test
    void findByServerName_isCaseInsensitive_andEmptyForUnknown() {
        givenCached(serverStatus("host-01", DriverStatus.OK, PowerSupplyStatus.OK, BatteryStatus.OK));

        assertTrue(statusQueryService.findByServerName("HOST-01").isPresent());
        assertTrue(statusQueryService.findByServerName("nope").isEmpty());
    }

    @Test
    void summary_countsBySeverityAndComponent() {
        givenCached(
                serverStatus("broken", DriverStatus.FAILED, PowerSupplyStatus.OK, BatteryStatus.OK),
                serverStatus("warned", DriverStatus.OK, PowerSupplyStatus.UNCLAIMED, BatteryStatus.OK),
                serverStatus("healthy", DriverStatus.OK, PowerSupplyStatus.OK, BatteryStatus.OK)
        );

        StatusSummaryResponse summary = statusQueryService.summary();

        assertEquals(3, summary.servers());
        assertEquals(1, summary.bySeverity().get(Severity.CRITICAL));
        assertEquals(1, summary.bySeverity().get(Severity.WARNING));
        assertEquals(1, summary.bySeverity().get(Severity.OK));
        assertEquals(1, summary.byComponent().get(HealthType.DRIVE_HEALTH).get(Severity.CRITICAL));
        assertEquals(2, summary.byComponent().get(HealthType.DRIVE_HEALTH).get(Severity.OK));
        assertEquals(List.of("broken", "warned"), summary.attentionRequired());
        assertEquals(Instant.parse("2026-08-21T10:00:00Z"), summary.generatedAt());
    }

    @Test
    void summary_emptyCache_returnsZeroCounters() {
        when(raidParserService.getCachedStatus()).thenReturn(List.of());

        StatusSummaryResponse summary = statusQueryService.summary();

        assertEquals(0, summary.servers());
        assertTrue(summary.attentionRequired().isEmpty());
        assertEquals(0, summary.bySeverity().get(Severity.CRITICAL));
    }
}
