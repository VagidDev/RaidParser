package com.unifun.raidparser.parser;

import com.unifun.raidparser.dto.ServerTask;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ServersToCheckConfigFileParser {
    private static final Logger LOGGER = LogManager.getLogger(ServersToCheckConfigFileParser.class);

    public List<ServerTask> parse(List<String> configData) {
        return configData.stream()
                .map(this::parseLine)
                .filter(Objects::nonNull)
                .toList();
    }

    private ServerTask parseLine(String configLine) {
        LOGGER.debug("Parsing line {}", configLine);
        String[] splitLine = configLine.split("->");
        if (splitLine.length == 2) {
            return new ServerTask(
                    splitLine[0].trim(),
                    splitLine[1].trim()
            );
        } else {
            LOGGER.warn("Cannot get server name and command from config line. Line to parse -> {}", configLine);
            return null;
        }
    }


}
