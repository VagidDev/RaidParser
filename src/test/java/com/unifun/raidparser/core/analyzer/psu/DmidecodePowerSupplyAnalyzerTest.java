package com.unifun.raidparser.core.analyzer.psu;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DmidecodePowerSupplyAnalyzerTest {
    private final DmidecodePowerSupplyAnalyzer analyzer = new DmidecodePowerSupplyAnalyzer();

    @Test
    void isSupportedRawData_returnsTrue_whenAllMarkersPresent() {
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

        assertTrue(analyzer.isSupportedRawData(text));
    }

    @Test
    void isSupportedRawData_returnsFalse_whenAnotherTypeOfPsuIsUsed() {
        String text = """
                Power Supply 1   | 3Dh | ok  | 10.1 | 50 Watts, Presence detected
                PS 1 Output      | 3Eh | ok  | 10.1 | 50 Watts
                PS 1 Presence    | 3Fh | ok  | 10.1 | Device Present
                Power Supply 2   | 40h | ok  | 10.2 | 25 Watts, Presence detected
                PS 2 Output      | 41h | ok  | 10.2 | 25 Watts
                PS 2 Presence    | 42h | ok  | 10.2 | Device Present
                Power Supplies   | 45h | ok  | 10.3 | Fully Redundant
                """;

        assertFalse(analyzer.isSupportedRawData(text));
    }

    @Test
    void getSupportedType_returnsPsuHealth() {
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

        AnalyzeResponse<PowerSupplyStatus> response = analyzer.analyze(text);

        assertEquals(PowerSupplyStatus.OK, response.getStatus());
        assertEquals("", response.getErrorText());
    }

    @Test
    void analyze_failedText_returnsFailed() {
        String text = """
                Power supply #1
                	Present  : Yes
                	Redundant: No
                	Condition: Ok
                	Hotplug  : Supported
                Power supply #2
                	Present  : Yes
                	Redundant: No
                	Condition: FAILED
                	Hotplug  : Supported
                """;

        AnalyzeResponse<PowerSupplyStatus> response = analyzer.analyze(text);

        assertEquals(PowerSupplyStatus.FAILED, response.getStatus());
        assertTrue(response.getErrorText().toLowerCase().contains("Condition: FAILED".toLowerCase()));
    }

    @Test
    void analyze_onePsuText_returnsNotPresent() {
        String text = """
                Power supply #1
                	Present  : Yes
                	Redundant: No
                	Condition: Ok
                	Hotplug  : Supported
                	Power    : 70 Watts
                Power supply #2
                	Power Supply not present
                """;

        AnalyzeResponse<PowerSupplyStatus> response = analyzer.analyze(text);

        assertEquals(PowerSupplyStatus.NOT_PRESENT, response.getStatus());
        assertTrue(response.getErrorText().toLowerCase().contains("Power Supply not present".toLowerCase()));
    }

    @Test
    void analyze_unclaimed_returnsUnclaimed() {
        String text = """
                *-power:0 UNCLAIMED
                     description: Power Supply 1
                     product: 720478-B21
                     vendor: HP
                     physical id: 1
                     serial: 5DMVV0C4D8S7VI
                     capacity: 500mWh
                *-power:1 UNCLAIMED
                     description: Power Supply 2
                     product: 720478-B21
                     vendor: HP
                     physical id: 2
                     serial: 5DMWA0CLL9C4DC
                     capacity: 500mWh
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