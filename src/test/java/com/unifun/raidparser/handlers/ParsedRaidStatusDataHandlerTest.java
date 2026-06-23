package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.ParsedRaidStatusDataCacheConfig;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.driver.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.service.RaidParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParsedRaidStatusDataHandlerTest {

    private static final Path REPORT_FILE = Path.of("/reports/current.txt");
    private static final Path OTHER_FILE  = Path.of("/reports/other.txt");
    private static final long TTL = 60L;

    @Mock
    private RaidParserService raidParserService;

    @Mock
    private ParsedRaidStatusDataCacheConfig cacheConfig;

    @InjectMocks
    private ParsedRaidStatusDataHandler handler;

    @BeforeEach
    void setUp() {
        when(cacheConfig.getDriveStatusAgeSeconds()).thenReturn(TTL);
        when(cacheConfig.getPowerSupplyStatusAgeSeconds()).thenReturn(TTL);
        when(cacheConfig.getBatteryStatusAgeSeconds()).thenReturn(TTL);

        // Имитируем вызов @PostConstruct вручную, т.к. Spring его не вызывает в unit-тестах
        ReflectionTestUtils.invokeMethod(handler, "initialize");
    }

    // ─────────────────────────────────────────────────────────────
    // Drive Status
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getSortedDriveStatus()")
    class DriveStatus {

        @Test
        @DisplayName("calls service on first request and returns result")
        void firstCall_invokesService_andReturnsData() {
            List<ServerStatus<DriverStatus>> expected = mockDriverList();
            when(raidParserService.getSortedDrivesStatus(REPORT_FILE)).thenReturn(expected);

            List<ServerStatus<DriverStatus>> result = handler.getSortedDriveStatus(REPORT_FILE);

            assertThat(result).isEqualTo(expected);
            verify(raidParserService, times(1)).getSortedDrivesStatus(REPORT_FILE);
        }

        @Test
        @DisplayName("returns cached data on second call — service not called again")
        void secondCall_returnsCachedData_withoutCallingService() {
            List<ServerStatus<DriverStatus>> data = mockDriverList();
            when(raidParserService.getSortedDrivesStatus(REPORT_FILE)).thenReturn(data);

            handler.getSortedDriveStatus(REPORT_FILE); // прогрев
            handler.getSortedDriveStatus(REPORT_FILE); // из кэша

            verify(raidParserService, times(1)).getSortedDrivesStatus(REPORT_FILE);
        }

        @Test
        @DisplayName("calls service again when file path changes")
        void differentFile_callsService_again() {
            when(raidParserService.getSortedDrivesStatus(any())).thenReturn(mockDriverList());

            handler.getSortedDriveStatus(REPORT_FILE);
            handler.getSortedDriveStatus(OTHER_FILE);

            verify(raidParserService, times(1)).getSortedDrivesStatus(REPORT_FILE);
            verify(raidParserService, times(1)).getSortedDrivesStatus(OTHER_FILE);
        }

        @Test
        @DisplayName("calls service again after TTL=0 expiry")
        void expiredCache_callsService_again() throws InterruptedException {
            // Пересоздаём handler с нулевым TTL
            when(cacheConfig.getDriveStatusAgeSeconds()).thenReturn(0L);
            ReflectionTestUtils.invokeMethod(handler, "initialize");

            when(raidParserService.getSortedDrivesStatus(REPORT_FILE)).thenReturn(mockDriverList());

            handler.getSortedDriveStatus(REPORT_FILE);
            Thread.sleep(1000);
            handler.getSortedDriveStatus(REPORT_FILE);

            verify(raidParserService, times(2)).getSortedDrivesStatus(REPORT_FILE);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Power Supply Status
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getSortedPowerSupplyStatus()")
    class PowerSupplyStatusTest {

        @Test
        @DisplayName("calls service on first request and returns result")
        void firstCall_invokesService_andReturnsData() {
            List<ServerStatus<PowerSupplyStatus>> expected = mockPowerList();
            when(raidParserService.getSortedPowerSuppliesStatus(REPORT_FILE)).thenReturn(expected);

            List<ServerStatus<PowerSupplyStatus>> result = handler.getSortedPowerSupplyStatus(REPORT_FILE);

            assertThat(result).isEqualTo(expected);
            verify(raidParserService, times(1)).getSortedPowerSuppliesStatus(REPORT_FILE);
        }

        @Test
        @DisplayName("returns cached data on second call — service not called again")
        void secondCall_returnsCachedData_withoutCallingService() {
            when(raidParserService.getSortedPowerSuppliesStatus(REPORT_FILE)).thenReturn(mockPowerList());

            handler.getSortedPowerSupplyStatus(REPORT_FILE);
            handler.getSortedPowerSupplyStatus(REPORT_FILE);

            verify(raidParserService, times(1)).getSortedPowerSuppliesStatus(REPORT_FILE);
        }

        @Test
        @DisplayName("calls service again when file path changes")
        void differentFile_callsService_again() {
            when(raidParserService.getSortedPowerSuppliesStatus(any())).thenReturn(mockPowerList());

            handler.getSortedPowerSupplyStatus(REPORT_FILE);
            handler.getSortedPowerSupplyStatus(OTHER_FILE);

            verify(raidParserService, times(1)).getSortedPowerSuppliesStatus(REPORT_FILE);
            verify(raidParserService, times(1)).getSortedPowerSuppliesStatus(OTHER_FILE);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Battery Status
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getSortedBatteryStatus()")
    class BatteryStatusTests {

        @Test
        @DisplayName("calls service on first request and returns result")
        void firstCall_invokesService_andReturnsData() {
            List<ServerStatus<BatteryStatus>> expected = mockBatteryList();
            when(raidParserService.getSortedBatteriesStatus(REPORT_FILE)).thenReturn(expected);

            List<ServerStatus<BatteryStatus>> result = handler.getSortedBatteryStatus(REPORT_FILE);

            assertThat(result).isEqualTo(expected);
            verify(raidParserService, times(1)).getSortedBatteriesStatus(REPORT_FILE);
        }

        @Test
        @DisplayName("returns cached data on second call — service not called again")
        void secondCall_returnsCachedData_withoutCallingService() {
            when(raidParserService.getSortedBatteriesStatus(REPORT_FILE)).thenReturn(mockBatteryList());

            handler.getSortedBatteryStatus(REPORT_FILE);
            handler.getSortedBatteryStatus(REPORT_FILE);

            verify(raidParserService, times(1)).getSortedBatteriesStatus(REPORT_FILE);
        }

        @Test
        @DisplayName("calls service again when file path changes")
        void differentFile_callsService_again() {
            when(raidParserService.getSortedBatteriesStatus(any())).thenReturn(mockBatteryList());

            handler.getSortedBatteryStatus(REPORT_FILE);
            handler.getSortedBatteryStatus(OTHER_FILE);

            verify(raidParserService, times(1)).getSortedBatteriesStatus(REPORT_FILE);
            verify(raidParserService, times(1)).getSortedBatteriesStatus(OTHER_FILE);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Изоляция кэшей
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Cache isolation")
    class CacheIsolation {

        @Test
        @DisplayName("caches for different status types are independent")
        void caches_areIndependentPerStatusType() {
            when(raidParserService.getSortedDrivesStatus(REPORT_FILE)).thenReturn(mockDriverList());
            when(raidParserService.getSortedPowerSuppliesStatus(REPORT_FILE)).thenReturn(mockPowerList());
            when(raidParserService.getSortedBatteriesStatus(REPORT_FILE)).thenReturn(mockBatteryList());

            // Заполняем все три кэша
            handler.getSortedDriveStatus(REPORT_FILE);
            handler.getSortedPowerSupplyStatus(REPORT_FILE);
            handler.getSortedBatteryStatus(REPORT_FILE);

            // Повторный вызов — каждый сервисный метод должен быть вызван ровно по 1 разу
            handler.getSortedDriveStatus(REPORT_FILE);
            handler.getSortedPowerSupplyStatus(REPORT_FILE);
            handler.getSortedBatteryStatus(REPORT_FILE);

            verify(raidParserService, times(1)).getSortedDrivesStatus(REPORT_FILE);
            verify(raidParserService, times(1)).getSortedPowerSuppliesStatus(REPORT_FILE);
            verify(raidParserService, times(1)).getSortedBatteriesStatus(REPORT_FILE);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<ServerStatus<DriverStatus>> mockDriverList() {
        return List.of(mock(ServerStatus.class));
    }

    @SuppressWarnings("unchecked")
    private List<ServerStatus<PowerSupplyStatus>> mockPowerList() {
        return List.of(mock(ServerStatus.class));
    }

    @SuppressWarnings("unchecked")
    private List<ServerStatus<BatteryStatus>> mockBatteryList() {
        return List.of(mock(ServerStatus.class));
    }
}