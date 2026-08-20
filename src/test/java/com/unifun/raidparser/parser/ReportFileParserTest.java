package com.unifun.raidparser.parser;

import com.unifun.raidparser.config.ReportFileDataBoundsPatternConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ServerData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportFileParserTest {
    private final ReportFileParser parser = new ReportFileParser(new ReportFileDataBoundsPatternConfig());

    private Path writeReport(Path dir, String content) throws IOException {
        Path report = dir.resolve("server_report");
        Files.writeString(report, content);
        return report;
    }

    @Test
    void readServerDataFromFile_nullPath_returnsEmptyList() {
        assertTrue(parser.readServerDataFromFile(null).isEmpty());
    }

    @Test
    void readServerDataFromFile_missingFile_returnsEmptyList(@TempDir Path tempDir) {
        assertTrue(parser.readServerDataFromFile(tempDir.resolve("no_such_file")).isEmpty());
    }

    @Test
    void readServerDataFromFile_readsEveryServer(@TempDir Path tempDir) throws IOException {
        Path report = writeReport(tempDir, """
                === SERVER NAME host-01
                =========================drive================================
                physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS, 300 GB, OK)
                ==========================RAM=================================
                === SERVER NAME host-02
                =========================drive================================
                physicaldrive 1I:3:2 (port 1I:box 3:bay 2, SAS, 300 GB, OK)
                ==========================RAM=================================
                """);

        List<ServerData> serverData = parser.readServerDataFromFile(report);

        assertEquals(2, serverData.size());
        assertEquals("host-01", serverData.get(0).serverName());
        assertEquals("host-02", serverData.get(1).serverName());
    }

    @Test
    void readServerDataFromFile_keepsLastLineOfReport(@TempDir Path tempDir) throws IOException {
        // Регрессия: последняя строка файла обрабатывалась как заголовок сервера,
        // поэтому статус последнего диска терялся.
        Path report = writeReport(tempDir, """
                === SERVER NAME host-01
                =========================drive================================
                physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS, 300 GB, OK)
                ==========================RAM=================================
                === SERVER NAME host-02
                =========================drive================================
                physicaldrive 1I:3:2 (port 1I:box 3:bay 2, SAS, 300 GB, Failed)""");

        List<ServerData> serverData = parser.readServerDataFromFile(report);

        assertEquals(2, serverData.size());
        ServerData lastServer = serverData.get(1);
        assertEquals("host-02", lastServer.serverName());
        assertTrue(lastServer.getRawData(HealthType.DRIVE_HEALTH).contains("failed"),
                "Последняя строка отчёта должна попадать в данные сервера: " + lastServer.getRawData(HealthType.DRIVE_HEALTH));
    }

    @Test
    void readServerDataFromFile_splitsDataByComponentBounds(@TempDir Path tempDir) throws IOException {
        Path report = writeReport(tempDir, """
                === SERVER NAME host-01
                =========================config===============================
                Battery/Capacitor Status: OK
                =========================drive================================
                physicaldrive 1I:3:1 (port 1I:box 3:bay 1, SAS, 300 GB, OK)
                ==========================RAM=================================
                ram data
                ==========================PSU=================================
                Condition: Ok
                =========================DIMM=================================
                """);

        ServerData serverData = parser.readServerDataFromFile(report).get(0);

        assertTrue(serverData.getRawData(HealthType.BATTERY_HEALTH).contains("battery/capacitor status: ok"));
        assertTrue(serverData.getRawData(HealthType.DRIVE_HEALTH).contains("physicaldrive"));
        assertTrue(serverData.getRawData(HealthType.PSU_HEALTH).contains("condition: ok"));
        assertFalse(serverData.getRawData(HealthType.DRIVE_HEALTH).contains("ram data"));
    }

    @Test
    void getMainData_lowercasesSingleLineSection() {
        // reduce() не применял toLowerCase к единственной строке,
        // а фильтры сравнивают текст в нижнем регистре.
        // строка-разделитель в секцию не входит, остаётся только содержимое
        String data = "start\nCondition: OK\nend";
        assertEquals("condition: ok", parser.getMainData(data, "start", "end"));
    }
}
