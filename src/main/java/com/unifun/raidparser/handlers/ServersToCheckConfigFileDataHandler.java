package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.dto.ServerTask;
import com.unifun.raidparser.parser.ServersToCheckConfigFileParser;
import com.unifun.raidparser.util.FileChecker;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServersToCheckConfigFileDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServersToCheckConfigFileDataHandler.class);
    private final ServersToCheckConfig serversToCheckConfig;
    private final ServersToCheckConfigFileParser serversToCheckConfigFileParser;
    private final FileChecker fileChecker;

    private List<ServerTask> serverTaskList;

    private boolean loadServerTasks() {
        Path configFile = Path.of(serversToCheckConfig.getServersToCheckConfigFile());
        if (!fileChecker.ensureFileExists(configFile)) {
            LOGGER.warn("Cannot load server tasks due to config file `{}` does not exist!", configFile);
            return false;
        }

        List<String> configData = readConfigFileData(configFile);
        if (CollectionUtils.isEmpty(configData)) {
            LOGGER.warn("Configuration file {} is empty!", configFile);
            return false;
        }
        // need to add a validator
        this.serverTaskList = serversToCheckConfigFileParser.parse(configData);
        return true;
    }

    private List<String> readConfigFileData(Path file) {
        try {
            LOGGER.info("Reading configuration file {}", file);
            return Files.readAllLines(file);
        } catch (IOException e) {
            LOGGER.error("Error while trying to read data from file {}", file);
            return null;
        }
    }

    public void clearCache() {
        if (serverTaskList != null) {
            LOGGER.warn("Cache is cleared!");
            serverTaskList.clear();
        }
    }

    public List<ServerTask> getServerTasks() {
        if (CollectionUtils.isEmpty(serverTaskList)) {
            if (loadServerTasks()) {
                LOGGER.info("Successfully load server task from config file");
            } else {
                LOGGER.warn("Cannot load server task from config file!");
            }
        }
        return serverTaskList;
    }

}
