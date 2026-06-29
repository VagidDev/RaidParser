package com.unifun.raidparser.parser;

import com.unifun.raidparser.dto.ServerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ReportFileParser {
    private static final Logger LOGGER = LogManager.getLogger(ReportFileParser.class);

    public synchronized List<ServerData> readServerDataFromFile(Path path) {
        if (path == null) {
            LOGGER.error("Empty file for reading servers data");
            return List.of();
        }

        List<ServerData> serverDataList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        String server = "";
        //=== SERVER NAME

        try (Stream<String> data = Files.lines(path)) {
            List<String> dataList = data.toList();

            for (int i = 0; i < dataList.size(); ++i) {
                if (dataList.get(i).contains("=== SERVER NAME") || i == (dataList.size() - 1)) {
                    //if new server
                    if (!server.isEmpty() && !builder.isEmpty()) {
                        serverDataList.add(new ServerData(server, builder.toString()));
                        server = "";
                        builder = new StringBuilder();
                    }

                    server = dataList.get(i).replace("=== SERVER NAME", " ");
                    server = server.trim();
                } else {
                    builder.append(dataList.get(i).trim()).append("\n");
                }

            }

            return serverDataList;
        } catch (IOException e) {
            LOGGER.error("Error while reading servers data from file `{}`. Error -> {}", path, e.getMessage(), e);
            return List.of();
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
}
