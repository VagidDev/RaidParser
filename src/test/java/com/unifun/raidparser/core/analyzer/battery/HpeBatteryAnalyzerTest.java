package com.unifun.raidparser.core.analyzer.battery;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HpeBatteryAnalyzerTest {

    private final HpeBatteryAnalyzer analyzer = new HpeBatteryAnalyzer();

    // =========================================================================
    // isSupportedRawData / getSupportedType / getUnknownStatus
    // =========================================================================

    @Test
    void isSupportedRawData_returnsTrue_whenBothMarkersPresent() {
        String text = "Smart Array P440ar in Slot 0\nCache Board Present: True";
        assertTrue(analyzer.isSupportedRawData(text));
    }

    @Test
    void isSupportedRawData_returnsFalse_whenCacheBoardMissing() {
        String text = "Smart Array P440ar in Slot 0\nCache Board Present: False";
        assertFalse(analyzer.isSupportedRawData(text));
    }

    @Test
    void getSupportedType_returnsBatteryHealth() {
        assertEquals(HealthType.BATTERY_HEALTH, analyzer.getSupportedType());
    }

    @Test
    void getUnknownStatus_returnsUnknown() {
        assertEquals(BatteryStatus.UNKNOWN, analyzer.getUnknownStatus());
    }

    // =========================================================================
    // analyze() - основная логика цепочки фильтров
    // =========================================================================

    @Test
    void analyze_blankText_returnsEmpty() {
        AnalyzeResponse<BatteryStatus> response = analyzer.analyze("   ");
        assertEquals(BatteryStatus.EMPTY, response.getStatus());
    }

    @Test
    void analyze_okText_returnsOk() {
        String text = """
                Smart Array P440ar in Slot 0
                   Cache Status: OK
                   No-Battery Write Cache: Disabled
                   Battery/Capacitor Status: OK
                """;

        AnalyzeResponse<BatteryStatus> response = analyzer.analyze(text);

        assertEquals(BatteryStatus.OK, response.getStatus());
        assertEquals("", response.getErrorText());
    }

    @Test
    void analyze_failedText_returnsFailed() {
        String text = """
                Smart Array P440ar in Slot 0
                   Cache Status: OK
                   Battery/Capacitor Status: Failed (Replace Batteries)
                """;

        AnalyzeResponse<BatteryStatus> response = analyzer.analyze(text);

        assertEquals(BatteryStatus.FAILED, response.getStatus());
        assertTrue(response.getErrorText().toLowerCase().contains("failed (replace batteries)"));
    }

    @Test
    void analyze_rechargingText_returnsRecharging() {
        String text = """
                Smart Array P440ar in Slot 0
                   Battery/Capacitor Status: Recharging
                """;

        AnalyzeResponse<BatteryStatus> response = analyzer.analyze(text);

        assertEquals(BatteryStatus.RECHARGING, response.getStatus());
    }

    @Test
    void analyze_batteryCountZero_returnsNoBattery() {
        String text = """
                Smart Array P440ar in Slot 0
                   Battery/Capacitor Count: 0
                """;

        AnalyzeResponse<BatteryStatus> response = analyzer.analyze(text);

        assertEquals(BatteryStatus.NO_BATTERY, response.getStatus());
    }

    @Test
    void analyze_noBatteryWriteCacheEnabledWithoutCapacitorCount_returnsNoBattery() {
        String text = """
                Smart Array P440ar in Slot 0
                   No-Battery Write Cache: Enabled
                """;

        AnalyzeResponse<BatteryStatus> response = analyzer.analyze(text);

        assertEquals(BatteryStatus.NO_BATTERY, response.getStatus());
    }

    @Test
    void analyze_cachePermanentlyDisabled_returnsCacheDisabled() {
        String text = """
                Smart Array P440ar in Slot 0
                   Cache Status: Permanently Disabled
                   Battery/Capacitor Status: OK
                """;

        AnalyzeResponse<BatteryStatus> response = analyzer.analyze(text);

        assertEquals(BatteryStatus.CACHE_DISABLED, response.getStatus());
    }

    @Test
    void analyze_noBatteryWriteCacheEnabledWithOkBattery_returnsNotSafe() {
        String text = """
                Smart Array P440ar in Slot 0
                   Battery/Capacitor Count: 1
                   No-Battery Write Cache: Enabled
                   Battery/Capacitor Status: OK
                """;

        AnalyzeResponse<BatteryStatus> response = analyzer.analyze(text);

        assertEquals(BatteryStatus.NOT_SAFE, response.getStatus());
    }

    @Test
    void analyze_unrecognizedText_returnsUnknownWithRawDataInErrorText() {
        String text = "some completely unrelated garbage output";

        AnalyzeResponse<BatteryStatus> response = analyzer.analyze(text);

        assertEquals(BatteryStatus.UNKNOWN, response.getStatus());
        assertTrue(response.getErrorText().contains(text));
    }

    // =========================================================================
    // Порядок фильтров: Failed должен иметь приоритет над Ok,
    // если в тексте одновременно присутствуют паттерны обоих фильтров.
    // =========================================================================

    @Test
    void analyze_textMatchingBothFailedAndOkPatterns_prefersFailedDueToFilterOrder() {
        String text = """
                Smart Array P440ar in Slot 0
                   Cache Status: OK
                   No-Battery Write Cache: Disabled
                   Battery/Capacitor Status: Failed (Replace Batteries)
                """;

        AnalyzeResponse<BatteryStatus> response = analyzer.analyze(text);

        assertEquals(BatteryStatus.FAILED, response.getStatus());
    }
}
