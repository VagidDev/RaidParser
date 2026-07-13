package com.unifun.raidparser.core.analyzer.psu;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IpmitoolPowerSupplyAnalyzerTest {
    private final IpmitoolPowerSupplyAnalyzer analyzer = new IpmitoolPowerSupplyAnalyzer();

    @Test
    void isSupportedRawData_returnsTrue_whenAllMarkersPresent() {
        String text = """
                Power Supply 1   | 3Dh | ok  | 10.1 | 50 Watts, Presence detected
                PS 1 Output      | 3Eh | ok  | 10.1 | 50 Watts
                PS 1 Presence    | 3Fh | ok  | 10.1 | Device Present
                Power Supply 2   | 40h | ok  | 10.2 | 25 Watts, Presence detected
                PS 2 Output      | 41h | ok  | 10.2 | 25 Watts
                PS 2 Presence    | 42h | ok  | 10.2 | Device Present
                Power Supplies   | 45h | ok  | 10.3 | Fully Redundant""";
        assertTrue(analyzer.isSupportedRawData(text));
    }

    @Test
    void isSupportedRawData_returnsFalse_whenAnotherTypeOfRaidIsUsed() {
        String text = """
                Power supply #1
                	Present  : Yes
                	Redundant: Yes
                	Condition: Ok
                	Hotplug  : Supported
                	Power    : 35 Watts
                Power supply #2
                	Present  : Yes
                	Redundant: Yes
                	Condition: Ok
                	Hotplug  : Supported
                	Power    : 45 Watts
                """;
        assertFalse(analyzer.isSupportedRawData(text));
    }

    @Test
    void getSupportedType_returnsDriveHealth() {
        assertEquals(HealthType.PSU_HEALTH, analyzer.getSupportedType());
    }

    @Test
    void getUnknownStatus_returnsUnknown() {
        assertEquals(PowerSupplyStatus.UNKNOWN, analyzer.getUnknownStatus());
    }

    // =========================================================================
    // analyze() - основная логика цепочки фильтров
    // =========================================================================

    @Test
    void analyze_blankText_returnsEmpty() {
        AnalyzeResponse<PowerSupplyStatus> response = analyzer.analyze("   ");
        assertEquals(PowerSupplyStatus.EMPTY, response.getStatus());
    }

    @Test
    void analyze_okText_returnsOk() {
        String text = """
                Power Supply 1   | 3Dh | ok  | 10.1 | 50 Watts, Presence detected
                PS 1 Output      | 3Eh | ok  | 10.1 | 50 Watts
                PS 1 Presence    | 3Fh | ok  | 10.1 | Device Present
                Power Supply 2   | 40h | ok  | 10.2 | 25 Watts, Presence detected
                PS 2 Output      | 41h | ok  | 10.2 | 25 Watts
                PS 2 Presence    | 42h | ok  | 10.2 | Device Present
                Power Supplies   | 45h | ok  | 10.3 | Fully Redundant
                """;

        AnalyzeResponse<PowerSupplyStatus> response = analyzer.analyze(text);

        assertEquals(PowerSupplyStatus.OK, response.getStatus());
        assertEquals("", response.getErrorText());
    }

    @Test
    void analyze_failedText_returnsFailed() {
        String text = """
                Power Supply 1   | 32h | ok  | 10.1 | Presence detected
                PS 1 Output      | 3Ah | ok  | 10.1 | 80 Watts
                Power Supply 2   | 33h | ok  | 10.2 | Presence detected, Failure detected, Power Supply AC lost
                PS 2 Output      | 3Bh | ok  | 10.2 | 0 Watts
                Power Supplies   | 42h | ok  | 19.1 | Redundancy Lost
                """;

        AnalyzeResponse<PowerSupplyStatus> response = analyzer.analyze(text);

        assertEquals(PowerSupplyStatus.FAILED, response.getStatus());
        assertTrue(response.getErrorText().toLowerCase().contains("Power Supply 2   | 33h | ok  | 10.2 | Presence detected, Failure detected, Power Supply AC lost".toLowerCase()));
    }

    @Test
    void analyze_onePSUText_returnsNotPresent() {
        String text = """
                Power Supply 1   | 32h | ok  | 10.1 | Presence detected
                PS 1 Output      | 3Ah | ok  | 10.1 | 40 Watts
                Power Supply 2   | 33h | ok  | 10.2 |\s
                PS 2 Output      | 3Bh | ns  | 10.2 | Disabled
                """;

        AnalyzeResponse<PowerSupplyStatus> response = analyzer.analyze(text);

        assertEquals(PowerSupplyStatus.NOT_PRESENT, response.getStatus());
        assertTrue(response.getErrorText().toLowerCase().contains("PS 2 Output      | 3Bh | ns  | 10.2 | Disabled".toLowerCase()));
    }

    @Test
    void analyze_unclaimed_returnsUnclaimed() {
        String text = """
                Power Supply 1   | 04h | lnc | 10.1 | 0 unspecified
                Power Supply 2   | 05h | lnc | 10.2 | 0 unspecified
                Power Supplies   | 06h | lnc | 10.3 | 0 unspecified
                """;

        AnalyzeResponse<PowerSupplyStatus> response = analyzer.analyze(text);

        assertEquals(PowerSupplyStatus.UNCLAIMED, response.getStatus());
    }


    @Test
    void analyze_unrecognizedText_returnsUnknownWithRawDataInErrorText() {
        String text = "some completely unrelated garbage output";

        AnalyzeResponse<PowerSupplyStatus> response = analyzer.analyze(text);

        assertEquals(PowerSupplyStatus.UNKNOWN, response.getStatus());
        assertTrue(response.getErrorText().contains(text));
    }

}