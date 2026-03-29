package com.unifun.raidparser.handlers;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ServerDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerDataHandler.class);
    private final RaidParserDataHandler raidParserDataHandler;

    private Map<String, String> serverData = new HashMap<>();
    private Path serverDataFile;

    public Map<String, String> getActualServerData(Path serverDataFile) {
        serverData = new HashMap<>();
        return getServerData(serverDataFile);
    }

    public Map<String, String> getServerData(Path serverDataFile) {
        if (serverDataFile == null) {
            LOGGER.warn("Cannot get server data from null path");
            return new HashMap<>();
        }

        if (this.serverDataFile == null || serverData.isEmpty() || !this.serverDataFile.equals(serverDataFile)) {
            this.serverDataFile = serverDataFile;
            LOGGER.info("Getting servers data from file `{}`. Servers count -> `{}`", serverDataFile, serverData.size());
            serverData = raidParserDataHandler.readServerDataFromFile(serverDataFile);
            return serverData;
        }

        LOGGER.info("Getting servers data from cache. Servers count -> `{}`", serverData.size());
        return serverData;
    }
}
