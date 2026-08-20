package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.CacheConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class ServerStatusDataHandlerTest {
    private static final Instant START = Instant.parse("2026-08-20T12:00:00Z");

    private static class TestClock extends Clock {
        private Instant now = START;

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }

        void advance(Duration duration) { now = now.plus(duration); }
    }

    private final TestClock clock = new TestClock();

    private ServerStatusDataHandler handler(long ttlSeconds) {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setServerStatusTtlSeconds(ttlSeconds);
        return new ServerStatusDataHandler(clock, cacheConfig);
    }

    private ServerStatus status(String serverName, DriverStatus driverStatus) {
        ConcurrentHashMap<HealthType, AnalyzeResponse<? extends Status>> map = new ConcurrentHashMap<>();
        map.put(HealthType.DRIVE_HEALTH, new AnalyzeResponse<>(driverStatus, ""));
        return new ServerStatus(serverName, map);
    }

    private DriverStatus driveStatusOf(ServerStatus serverStatus) {
        return (DriverStatus) serverStatus.healthStatusMap().get(HealthType.DRIVE_HEALTH).getStatus();
    }

    @Test
    void getAll_dropsEntriesOlderThanTtl() {
        ServerStatusDataHandler handler = handler(60);
        handler.updateStatus(status("host-01", DriverStatus.OK));

        clock.advance(Duration.ofSeconds(59));
        assertEquals(1, handler.getAll().size());

        clock.advance(Duration.ofSeconds(1));
        assertTrue(handler.getAll().isEmpty());
        assertNull(handler.get("host-01"));
    }

    @Test
    void freshStatusReplacesExpiredOne_insteadOfMergingByPriority() {
        // Пока запись жива, слияние оставляет более серьёзный статус;
        // после истечения TTL починенный сервер должен снова стать OK.
        ServerStatusDataHandler handler = handler(60);
        handler.updateStatus(status("host-01", DriverStatus.FAILED));

        handler.updateStatus(status("host-01", DriverStatus.OK));
        assertEquals(DriverStatus.FAILED, driveStatusOf(handler.get("host-01")));

        clock.advance(Duration.ofSeconds(61));
        handler.updateStatus(status("host-01", DriverStatus.OK));

        assertEquals(DriverStatus.OK, driveStatusOf(handler.get("host-01")));
    }

    @Test
    void nonPositiveTtl_keepsStatusesForever() {
        ServerStatusDataHandler handler = handler(0);
        handler.updateStatus(status("host-01", DriverStatus.OK));

        clock.advance(Duration.ofDays(30));

        assertEquals(1, handler.getAll().size());
    }

    @Test
    void state_reportsSizeAgeAndExpiration() {
        ServerStatusDataHandler handler = handler(60);
        handler.updateStatus(status("host-01", DriverStatus.OK));
        clock.advance(Duration.ofSeconds(30));
        handler.updateStatus(status("host-02", DriverStatus.OK));

        CacheState state = handler.state();

        assertEquals("status", state.name());
        assertTrue(state.loaded());
        assertEquals(2, state.size());
        // возраст считается по самой старой записи
        assertEquals(Duration.ofSeconds(30), state.age());
        assertEquals(60, state.ttlSeconds());
        assertFalse(state.expired());

        clock.advance(Duration.ofSeconds(31));
        assertTrue(handler.state().expired());
    }

    @Test
    void clear_removesEverything() {
        ServerStatusDataHandler handler = handler(60);
        handler.updateAll(List.of(status("host-01", DriverStatus.OK), status("host-02", DriverStatus.FAILED)));

        handler.clear();

        assertTrue(handler.getAll().isEmpty());
        assertFalse(handler.state().loaded());
    }
}
