package com.unifun.raidparser.handlers;

import com.unifun.raidparser.analyzer.response.AnalyzeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FileDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(FileDataHandler.class);

    public static synchronized Map<String, String> readServerDataFromFile(String path) {
        if (path.isEmpty()) {
            LOGGER.error("Empty file for reading servers data `{}`", path);
            return new HashMap<>();
        }

        Map<String, String> servers = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        String server = "";
        //=== SERVER NAME

        try (Stream<String> data = Files.lines(Path.of(path))) {
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

    public static String getMainData(String data, String startPattern, String endPattern) {
        return data.lines()
                .takeWhile(line -> !line.contains(endPattern))
                .dropWhile(line -> !line.contains(startPattern))
                .filter(str -> !str.equals(startPattern))
                .reduce((x, y) -> x.toLowerCase() + "\n" + y.toLowerCase())
                .orElse("");
    }

    public static <T> void writeData(String path, List<Map.Entry<String, AnalyzeResponse<T>>> list) {
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
        }

        try {
            Path filePath = Path.of(path);
            if (!Files.isRegularFile(filePath)) {
                //check if directory exists, if not - create dir
                if (Files.notExists(filePath.getParent())) {
                    Files.createDirectory(filePath.getParent());
                }
                // create file
                Files.createFile(filePath);
            }
            Files.writeString(filePath, builder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
