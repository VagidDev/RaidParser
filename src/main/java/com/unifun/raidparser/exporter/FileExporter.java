package com.unifun.raidparser.exporter;

import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.dto.ServerStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//TODO: write tests for this class
@Service
public class FileExporter {
    private static final Logger LOGGER = LogManager.getLogger(FileExporter.class);

    private boolean ensureFileExists(Path path) {
        try {
            if (!Files.isRegularFile(path)) {
                //check if directory exists, if not - create dir
                if (Files.notExists(path.getParent())) {
                    LOGGER.warn("Directory {} does not exist. Creating directory...", path.getParent());
                    Files.createDirectory(path.getParent());
                }
                // create file
                Files.createFile(path);
                LOGGER.warn("File {} does not exist. Creating file...", path);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while trying to create file {}. Error -> {}", path, e.getLocalizedMessage(), e);
            return false;
        }
    }

    public <T extends Status> void export(Path path, List<ReportServerData> reportServerDataList) {
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
        if (ensureFileExists(path)) {
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
