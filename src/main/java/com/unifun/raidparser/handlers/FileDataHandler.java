package com.unifun.raidparser.handlers;

import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class FileDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(FileDataHandler.class);

    public synchronized Map<String, String> readServerDataFromFile(Path path) {
        if (path == null) {
            LOGGER.error("Empty file for reading servers data");
            return new HashMap<>();
        }

        Map<String, String> servers = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        String server = "";
        //=== SERVER NAME

        try (Stream<String> data = Files.lines(path)) {
            List<String> dataList = data.toList();

            for (int i = 0; i < dataList.size(); ++i) {
                if (dataList.get(i).contains("=== SERVER NAME") || i == (dataList.size() - 1)) {
                    //if new server
                    if (!server.isEmpty() && !builder.isEmpty()) {
                        servers.put(server, builder.toString());
                        server = "";
                        builder = new StringBuilder();
                    }

                    server = dataList.get(i).replace("=== SERVER NAME", " ");
                    server = server.trim();
                } else {
                    builder.append(dataList.get(i).trim()).append("\n");
                }

            }

            return servers;
        } catch (IOException e) {
            LOGGER.error("Error while reading servers data from file `{}`. Error -> {}", path, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    public String getMainData(String data, String startPattern, String endPattern) {
        return data.lines()
                .takeWhile(line -> !line.contains(endPattern))
                .dropWhile(line -> !line.contains(startPattern))
                .filter(str -> !str.equals(startPattern))
                .reduce((x, y) -> x.toLowerCase() + "\n" + y.toLowerCase())
                .orElse("");
    }

    public <T> void writeData(Path path, List<Map.Entry<String, AnalyzeResponse<T>>> list) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, AnalyzeResponse<T>> entry : list) {
            builder
                    .append(entry.getKey()).append(" -> ")
                    .append(entry.getValue().getStatus())
                    .append("\n");

            if (!entry.getValue().getErrorText().isBlank()) {
                builder
                        .append("------------------\n")
                        .append(entry.getValue().getErrorText())
                        .append("====================\n");
            }

            if (ensureFileExists(path)) {
                LOGGER.info("Writing data to file {}", path);
                writeToFile(path, builder.toString());
            } else {
                LOGGER.error("Cannot write data to file {} because it is does not exist", path);
            }
        }
    }

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

    private void writeToFile(Path path, String data) {
        try {
            Files.writeString(path, data);
        } catch (IOException e) {
            LOGGER.error("Error while trying to write data to file {}. Error -> {}", path, e.getLocalizedMessage(), e);
        }
    }
}
