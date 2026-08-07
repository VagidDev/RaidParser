package com.unifun.raidparser.core.analyzer.drive;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.drive.DriveEmptyFilter;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.filters.drive.ssacli.DriveFailedFilter;
import com.unifun.raidparser.core.filters.drive.ssacli.DriveOkFilter;
import com.unifun.raidparser.core.filters.drive.ssacli.DriverInterimRecoveryModeFilter;
import com.unifun.raidparser.core.filters.drive.ssacli.DriverPredictiveFailureFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SsacliDriveAnalyzerTest {
    private SsacliDriveAnalyzer analyzer;

    public SsacliDriveAnalyzerTest() {
        initialize();
    }
    private void initialize() {
        analyzer = new SsacliDriveAnalyzer(
                List.of(
                        new DriverPredictiveFailureFilter(),
                        new DriveFailedFilter(),
                        new DriveOkFilter(),
                        new DriverInterimRecoveryModeFilter(),
                        new DriveEmptyFilter()
                )
        );
    }

    @Test
    void isSupportedRawData_returnsTrue_whenBothMarkersPresent() {
        String text = "logicaldrive 1 (279.37 GB, RAID 1, OK)\n" +
                "      physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS HDD, 300 GB, OK)\n" +
                "      physicaldrive 1I:3:2 (port 1I:box 3:bay 2, SAS HDD, 300 GB, OK)\n" +
                "      physicaldrive 1I:3:3 (port 1I:box 3:bay 3, SAS HDD, 300 GB, OK, auto replace spare)";
        assertTrue(analyzer.isSupportedRawData(text));
    }

    @Test
    void isSupportedRawData_returnsFalse_whenAnotherTypeOfRaidIsUsed() {
        String text = "[stdout] Personalities : [raid1] [linear] [multipath] [raid0] [raid6] [raid5] [raid4] [raid10] \n" +
                "[stdout] md2 : active raid1 sdd1[3] sdc1[2]\n" +
                "[stdout]       488254336 blocks super 1.2 [2/2] [UU]\n" +
                "[stdout]       \n" +
                "[stdout] md128 : active raid1 sdb1[1] sda1[0]\n" +
                "[stdout]       976629760 blocks super 1.2 [2/2] [UU]\n" +
                "[stdout]       bitmap: 5/8 pages [20KB], 65536KB chunk\n" +
                "[stdout] \n" +
                "[stdout] unused devices: <none>\n";
        assertFalse(analyzer.isSupportedRawData(text));
    }

    @Test
    void getSupportedType_returnsDriveHealth() {
        assertEquals(HealthType.DRIVE_HEALTH, analyzer.getSupportedType());
    }

    @Test
    void getUnknownStatus_returnsUnknown() {
        assertEquals(DriverStatus.UNKNOWN, analyzer.getUnknownStatus());
    }

    // =========================================================================
    // analyze() - основная логика цепочки фильтров
    // =========================================================================

    @Test
    void analyze_blankText_returnsEmpty() {
        AnalyzeResponse<DriverStatus> response = analyzer.analyze("   ");
        assertEquals(DriverStatus.EMPTY, response.getStatus());
    }

    @Test
    void analyze_okText_returnsOk() {
        String text = """
                logicaldrive 1 (558.73 GB, RAID 1+0, OK)
                    physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS HDD, 300 GB, OK)
                    physicaldrive 1I:3:2 (port 1I:box 3:bay 2, SAS HDD, 300 GB, OK)
                    physicaldrive 1I:3:3 (port 1I:box 3:bay 3, SAS HDD, 300 GB, OK)
                    physicaldrive 1I:3:4 (port 1I:box 3:bay 4, SAS HDD, 300 GB, OK)
                    physicaldrive 2I:3:5 (port 2I:box 3:bay 5, SAS HDD, 300 GB, OK, spare)
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.OK, response.getStatus());
        assertEquals("", response.getErrorText());
    }

    @Test
    void analyze_failedText_returnsFailed() {
        String text = """
                logicaldrive 1 (136.7 GB, RAID 1, OK)
                    physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS, 146 GB, OK)
                    physicaldrive 1I:3:2 (port 1I:box 3:bay 2, SAS, 146 GB, OK)
                    physicaldrive 2I:3:7 (port 2I:box 3:bay 7, SAS, 600 GB, OK, spare)
                logicaldrive 2 (558.7 GB, RAID 1+0, OK)
                    physicaldrive 1I:3:3 (port 1I:box 3:bay 3, SAS, 300 GB, Failed)
                    physicaldrive 1I:3:4 (port 1I:box 3:bay 4, SAS, 600 GB, OK)
                    physicaldrive 2I:3:5 (port 2I:box 3:bay 5, SAS, 300 GB, OK)
                    physicaldrive 2I:3:6 (port 2I:box 3:bay 6, SAS, 300 GB, OK)
                    physicaldrive 2I:3:8 (port 2I:box 3:bay 8, SAS, 600 GB, OK, active spare for 1I:3:3)
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.FAILED, response.getStatus());
        assertTrue(response.getErrorText().toLowerCase().contains("physicaldrive 1I:3:3 (port 1I:box 3:bay 3, SAS, 300 GB, Failed)".toLowerCase()));
    }

    @Test
    void analyze_recoveryText_returnsInterimRecoveryMode() {
        String text = """
                logicaldrive 1 (136.7 GB, RAID 1, OK)
                    physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS, 146 GB, OK)
                    physicaldrive 1I:3:2 (port 1I:box 3:bay 2, SAS, 146 GB, OK)
                    physicaldrive 2I:3:7 (port 2I:box 3:bay 7, SAS, 600 GB, OK, spare)
                logicaldrive 2 (558.7 GB, RAID 1+0, Interim Recovery Mode)
                    physicaldrive 1I:3:3 (port 1I:box 3:bay 3, SAS, 300 GB, Failed)
                    physicaldrive 1I:3:4 (port 1I:box 3:bay 4, SAS, 600 GB, OK)
                    physicaldrive 2I:3:5 (port 2I:box 3:bay 5, SAS, 300 GB, Failed)
                    physicaldrive 2I:3:6 (port 2I:box 3:bay 6, SAS, 300 GB, OK)
                    physicaldrive 2I:3:8 (port 2I:box 3:bay 8, SAS, 600 GB, OK, active spare for 1I:3:3)
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.INTERIM_RECOVERY_MODE, response.getStatus());
        assertTrue(response.getErrorText().toLowerCase().contains("logicaldrive 2 (558.7 GB, RAID 1+0, Interim Recovery Mode)".toLowerCase()));
    }

    @Test
    void analyze_predictiveFailure_returnsPredictiveFailure() {
        String text = """
                logicaldrive 1 (558.7 GB, RAID 1+0, OK)
                      physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS, 300 GB, OK)
                      physicaldrive 1I:3:2 (port 1I:box 3:bay 2, SAS, 300 GB, OK)
                      physicaldrive 1I:3:3 (port 1I:box 3:bay 3, SAS, 300 GB, OK)
                      physicaldrive 1I:3:4 (port 1I:box 3:bay 4, SAS, 300 GB, Predictive Failure)
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.PREDICTIVE_FAILURE, response.getStatus());
    }


    @Test
    void analyze_unrecognizedText_returnsUnknownWithRawDataInErrorText() {
        String text = "some completely unrelated garbage output";

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.UNKNOWN, response.getStatus());
        assertTrue(response.getErrorText().contains(text));
    }

    // =========================================================================
    // Порядок фильтров: Failed должен иметь приоритет над Ok,
    // если в тексте одновременно присутствуют паттерны обоих фильтров.
    // =========================================================================

    @Test
    void analyze_textMatchingBothFailedAndPredictiveFailurePatterns_prefersFailed() {
        String text = """
                logicaldrive 1 (136.7 GB, RAID 1, OK)
                    physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS, 146 GB, OK)
                    physicaldrive 1I:3:2 (port 1I:box 3:bay 2, SAS, 146 GB, OK)
                    physicaldrive 2I:3:7 (port 2I:box 3:bay 7, SAS, 600 GB, OK, spare)
                logicaldrive 2 (558.7 GB, RAID 1+0, OK)
                    physicaldrive 1I:3:3 (port 1I:box 3:bay 3, SAS, 300 GB, Failed)
                    physicaldrive 1I:3:4 (port 1I:box 3:bay 4, SAS, 600 GB, OK)
                    physicaldrive 2I:3:5 (port 2I:box 3:bay 5, SAS, 300 GB, OK)
                    physicaldrive 1I:3:4 (port 1I:box 3:bay 4, SAS, 300 GB, Predictive Failure)
                    physicaldrive 2I:3:8 (port 2I:box 3:bay 8, SAS, 600 GB, OK, active spare for 1I:3:3)
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.FAILED, response.getStatus());
    }
}