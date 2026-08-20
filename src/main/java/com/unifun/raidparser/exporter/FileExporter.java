package com.unifun.raidparser.exporter;

import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.util.FileChecker;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileExporter {
    private static final Logger LOGGER = LogManager.getLogger(FileExporter.class);

    // Создание файла вместе с деревом каталогов уже реализовано в FileChecker:
    // локальная копия умела только один уровень вложенности и падала на пути без родителя.
    private final FileChecker fileChecker;

    public void export(Path path, List<ReportServerData> reportServerDataList) {
        StringBuilder builder = new StringBuilder();
        for (ReportServerData reportServerData : reportServerDataList) {
            builder
                    .append(reportServerData.serverName()).append(" -> ")
                    .append(reportServerData.healthStatus())
                    .append("\n");

            if (!reportServerData.errorText().isBlank()) {
                builder
                        .append("------------------\n")
                        .append(reportServerData.errorText())
                        .append("\n====================\n");
            }
        }
        if (fileChecker.ensureFileExists(path)) {
            LOGGER.info("Writing data to file {}", path);
            writeToFile(path, builder.toString());
        } else {
            LOGGER.error("Cannot write data to file {} because it is does not exist", path);
        }
    }

    private void writeToFile(Path path, String data) {
        try {
            Files.writeString(path, data);
        } catch (IOException e) {
            LOGGER.error("Error while trying to write data to file {}. Error -> {}", path, e.getLocalizedMessage(), e);
        }
    }
}
