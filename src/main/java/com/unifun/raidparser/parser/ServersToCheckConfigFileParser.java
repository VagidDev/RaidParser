package com.unifun.raidparser.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.unifun.raidparser.config.ServerTasksConfig;
import com.unifun.raidparser.dto.HostCommand;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServersToCheckConfigFileParser {
    private static final Logger LOGGER = LogManager.getLogger(ServersToCheckConfigFileParser.class);

    private ServerTasksConfig readConfig(Path configFile) throws IOException {
        LOGGER.info("Parsing config file {}", configFile.toString());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(configFile.toFile(), ServerTasksConfig.class);
    }

    public List<HostCommand> parse(Path configFile) {
        try {
            ServerTasksConfig serverTasksConfig = readConfig(configFile);
            if (serverTasksConfig == null) {
                LOGGER.warn("ServerTasksConfig is null");
                return List.of();
            } else if (CollectionUtils.isEmpty(serverTasksConfig.getCommands())) {
                LOGGER.warn("No commands in config file `{}`", configFile.toString());
                return List.of();
            }

            return serverTasksConfig.getCommands();
        } catch (Exception exception) {
            LOGGER.error("Error while reading config file `{}`. Error -> {}", configFile, exception.getMessage(), exception);
            return List.of();
        }
    }


}
