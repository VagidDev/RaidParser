package com.unifun.raidparser.parser;

import com.unifun.raidparser.config.ReportFileDataBoundsPatternConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ServerData;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReportFileParser {
    private static final Logger LOGGER = LogManager.getLogger(ReportFileParser.class);
    private static final String SERVER_NAME_MARKER = "=== SERVER NAME";

    private final ReportFileDataBoundsPatternConfig reportFileDataBoundsPatternConfig;

    public synchronized List<ServerData> readServerDataFromFile(Path path) {
        if (path == null) {
            LOGGER.error("Empty file for reading servers data");
            return List.of();
        }

        List<ServerData> serverDataList = new ArrayList<>();

        try (Stream<String> data = Files.lines(path)) {
            String server = "";
            StringBuilder statusDetail = new StringBuilder();

            for (String line : data.toList()) {
                if (line.contains(SERVER_NAME_MARKER)) {
                    addServerData(serverDataList, server, statusDetail);
                    server = line.replace(SERVER_NAME_MARKER, " ").trim();
                    statusDetail = new StringBuilder();
                } else {
                    statusDetail.append(line.trim()).append("\n");
                }
            }
            // Последний сервер в файле не закрыт новым заголовком, поэтому
            // сбрасываем его после цикла: иначе терялась последняя строка отчёта.
            addServerData(serverDataList, server, statusDetail);

            LOGGER.debug("Read server data from file `{}`, server data -> {}", path, serverDataList);
            return serverDataList;
        } catch (IOException e) {
            LOGGER.error("Error while reading servers data from file `{}`. Error -> {}", path, e.getMessage(), e);
            return List.of();
        }
    }

    private void addServerData(List<ServerData> serverDataList, String serverName, StringBuilder statusDetail) {
        if (serverName.isEmpty() || statusDetail.isEmpty()) {
            return;
        }

        String data = statusDetail.toString();
        serverDataList.add(new ServerData(
                serverName,
                Map.of(
                        HealthType.DRIVE_HEALTH, getMainData(data, reportFileDataBoundsPatternConfig.getDriveStart(), reportFileDataBoundsPatternConfig.getDriveEnd()),
                        HealthType.PSU_HEALTH, getMainData(data, reportFileDataBoundsPatternConfig.getPsuStart(), reportFileDataBoundsPatternConfig.getPsuEnd()),
                        HealthType.BATTERY_HEALTH, getMainData(data, reportFileDataBoundsPatternConfig.getBatteryStart(), reportFileDataBoundsPatternConfig.getBatteryEnd())
                )
        ));
    }

    public String getMainData(String data, String startPattern, String endPattern) {
        return data.lines()
                .takeWhile(line -> !line.contains(endPattern))
                .dropWhile(line -> !line.contains(startPattern))
                .filter(str -> !str.equals(startPattern))
                .map(String::toLowerCase)
                .collect(Collectors.joining("\n"));
    }
}
