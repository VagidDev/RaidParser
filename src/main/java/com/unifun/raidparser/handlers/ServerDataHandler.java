package com.unifun.raidparser.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class ServerDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerDataHandler.class);

    private Map<String, String> serverData = new HashMap<>();
    private String serverDataFile = "";

    public Map<String, String> getActualServerData(String serverDataFile) {
        serverData = new HashMap<>();
        return getServerData(serverDataFile);
    }

    public Map<String, String> getServerData(String serverDataFile) {
        if (this.serverDataFile.equals(serverDataFile)
                && !this.serverData.isEmpty()) {
            LOGGER.debug("Getting servers data from cache. Servers count -> `{}`", serverData.size());
            return serverData;
        }

        this.serverDataFile = serverDataFile;
        LOGGER.debug("Getting servers data from file `{}`. Servers count -> `{}`", serverDataFile, serverData.size());
        serverData = FileDataHandler.readServerDataFromFile(serverDataFile);
        return serverData;
    }
}
