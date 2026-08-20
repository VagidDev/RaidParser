package com.unifun.raidparser.core.analyzer.drive;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.drive.DriveEmptyFilter;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.filters.drive.mdadm.DriveDegradedFilter;
import com.unifun.raidparser.core.filters.drive.mdadm.DriveOkFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MdadmDriveAnalyzerTest {
    private MdadmDriveAnalyzer analyzer;

    public MdadmDriveAnalyzerTest() {
        initialize();
    }

    private void initialize() {
        analyzer = new MdadmDriveAnalyzer(
                List.of(
                        new DriveOkFilter(),
                        new DriveDegradedFilter(),
                        new DriveEmptyFilter()
                )
        );
    }

    @Test
    void isSupportedRawData_returnsTrue_whenAllMarkersPresent() {
        String text = """
                [stdout] Personalities : [raid1] [linear] [multipath] [raid0] [raid6] [raid5] [raid4] [raid10]\s
                [stdout] md2 : active raid1 sdd1[3] sdc1[2]
                [stdout]       488254336 blocks super 1.2 [2/2] [UU]
                [stdout]      \s
                [stdout] md128 : active raid1 sdb1[1] sda1[0]
                [stdout]       976629760 blocks super 1.2 [2/2] [UU]
                [stdout]       bitmap: 5/8 pages [20KB], 65536KB chunk
                [stdout]\s
                [stdout] unused devices: <none>
                """;
        assertTrue(analyzer.isSupportedRawData(text));
    }

    @Test
    void isSupportedRawData_returnsFalse_whenAnotherTypeOfRaidIsUsed() {
        String text = """
                logicaldrive 1 (558.7 GB, RAID 1+0, OK)
                      physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS, 300 GB, OK)
                      physicaldrive 1I:3:2 (port 1I:box 3:bay 2, SAS, 300 GB, OK)
                      physicaldrive 1I:3:3 (port 1I:box 3:bay 3, SAS, 300 GB, OK)
                      physicaldrive 1I:3:4 (port 1I:box 3:bay 4, SAS, 300 GB, Predictive Failure)
                """;
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
                [stdout] Personalities : [raid1] [linear] [multipath] [raid0] [raid6] [raid5] [raid4] [raid10]\s
                [stdout] md2 : active raid1 sdd1[3] sdc1[2]
                [stdout]       488254336 blocks super 1.2 [2/2] [UU]
                [stdout]      \s
                [stdout] md128 : active raid1 sdb1[1] sda1[0]
                [stdout]       976629760 blocks super 1.2 [2/2] [UU]
                [stdout]       bitmap: 5/8 pages [20KB], 65536KB chunk
                [stdout]\s
                [stdout] unused devices: <none>
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.OK, response.getStatus());
        assertEquals("", response.getErrorText());
    }

    @Test
    void analyze_failedText_returnsDegraded() {
        String text = """
                [stdout] Personalities: [raid1] [linear] [multipath] [raid0] [raid6] 
                [stdout] md2 : active raid1 sdd1[1] sdc1[0](F)
                [stdout]     488254336 blocks super 1.2 [2/1] [_U]
                [stdout]      \s
                [stdout] md128 : active raid1 sdb1 [1] sda1[0]
                [stdout]     976629760 blocks super 1.2 [2/2] [UU] bitmap: 3/8 pages [12KB], 65536KB chunk
                [stdout]      \s
                [stdout] unused devices: <none>
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.DEGRADED, response.getStatus());
        assertTrue(response.getErrorText().toLowerCase().contains("blocks super 1.2 [2/1] [_U]".toLowerCase()));
    }


    // =========================================================================
    // Не только raid1: формат `[всего/живых] [UU_]` одинаков для всех уровней
    // =========================================================================

    @Test
    void analyze_healthyRaid10_returnsOk() {
        String text = """
                [stdout] Personalities : [raid10] [raid6] [raid5] [raid4]\s
                [stdout] md0 : active raid10 sdd1[3] sdc1[2] sdb1[1] sda1[0]
                [stdout]       1953260544 blocks super 1.2 512K chunks 2 near-copies [4/4] [UUUU]
                [stdout]\s
                [stdout] unused devices: <none>
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.OK, response.getStatus());
    }

    @Test
    void analyze_degradedRaid5_returnsDegraded() {
        String text = """
                [stdout] Personalities : [raid6] [raid5] [raid4]\s
                [stdout] md0 : active raid5 sdd1[3] sdc1[2](F) sdb1[1] sda1[0]
                [stdout]       5860147200 blocks super 1.2 level 5, 512k chunk, algorithm 2 [4/3] [UU_U]
                [stdout]\s
                [stdout] unused devices: <none>
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.DEGRADED, response.getStatus());
        assertTrue(response.getErrorText().contains("[4/3] [UU_U]"));
    }

    @Test
    void analyze_inactiveArray_returnsDegraded() {
        // Полностью развалившийся массив: строки состояния нет вообще,
        // раньше такой вывод не подходил ни одному фильтру и уходил в UNKNOWN.
        String text = """
                [stdout] Personalities : [raid1]\s
                [stdout] md127 : inactive sda1[0](S)
                [stdout]       488254336 blocks super 1.2
                [stdout]\s
                [stdout] unused devices: <none>
                """;

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.DEGRADED, response.getStatus());
    }

    @Test
    void isSupportedRawData_returnsTrue_forRaid5Output() {
        String text = """
                [stdout] Personalities : [raid6] [raid5] [raid4]\s
                [stdout] md0 : active raid5 sdd1[3] sdc1[2] sdb1[1] sda1[0]
                [stdout]       5860147200 blocks super 1.2 level 5, 512k chunk, algorithm 2 [4/4] [UUUU]
                """;
        assertTrue(analyzer.isSupportedRawData(text));
    }

    @Test
    void analyze_unrecognizedText_returnsUnknownWithRawDataInErrorText() {
        String text = "some completely unrelated garbage output";

        AnalyzeResponse<DriverStatus> response = analyzer.analyze(text);

        assertEquals(DriverStatus.UNKNOWN, response.getStatus());
        assertTrue(response.getErrorText().contains(text));
    }
}