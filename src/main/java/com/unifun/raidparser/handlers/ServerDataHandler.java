package com.unifun.raidparser.handlers;

import com.unifun.raidparser.dto.ServerData;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ServerDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerDataHandler.class);
    private final RaidParserDataHandler raidParserDataHandler;

    private List<ServerData> serverData = List.of();
    private Path serverDataFile;

    public List<ServerData> getActualServerData(Path serverDataFile) {
        serverData = List.of();
        return getServerData(serverDataFile);
    }

    public List<ServerData> getServerData(Path serverDataFile) {
        if (serverDataFile == null) {
            LOGGER.warn("Cannot get server data from null path");
            return List.of();
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
