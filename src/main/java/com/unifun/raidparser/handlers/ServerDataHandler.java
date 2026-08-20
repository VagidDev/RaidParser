package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.CacheConfig;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.parser.ReportFileParser;
import com.unifun.raidparser.util.CacheExpiry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

@Component
public class ServerDataHandler implements ManagedCache {
    private static final Logger LOGGER = LogManager.getLogger(ServerDataHandler.class);
    private static final String CACHE_NAME = "report";

    private final ReportFileParser reportFileParser;
    private final CacheExpiry expiry;

    private List<ServerData> serverData = List.of();
    private Path serverDataFile;

    public ServerDataHandler(ReportFileParser reportFileParser, Clock clock, CacheConfig cacheConfig) {
        this.reportFileParser = reportFileParser;
        this.expiry = new CacheExpiry(clock, cacheConfig.getReportDataTtlSeconds());
    }

    public List<ServerData> getActualServerData(Path serverDataFile) {
        clear();
        return getServerData(serverDataFile);
    }

    public List<ServerData> getServerData(Path serverDataFile) {
        if (serverDataFile == null) {
            LOGGER.warn("Cannot get server data from null path");
            return List.of();
        }

        if (needsReload(serverDataFile)) {
            this.serverDataFile = serverDataFile;
            LOGGER.info("Getting servers data from file `{}`", serverDataFile);
            serverData = reportFileParser.readServerDataFromFile(serverDataFile);
            expiry.markLoaded();
            return serverData;
        }

        LOGGER.info("Getting servers data from cache. Servers count -> `{}`", serverData.size());
        return serverData;
    }

    @Override
    public String cacheName() {
        return CACHE_NAME;
    }

    @Override
    public void clear() {
        serverData = List.of();
        serverDataFile = null;
        expiry.invalidate();
        LOGGER.info("Report data cache is cleared");
    }

    @Override
    public CacheState state() {
        return new CacheState(
                CACHE_NAME,
                !serverData.isEmpty(),
                serverData.size(),
                expiry.age(),
                expiry.ttlSeconds(),
                expiry.isExpired()
        );
    }

    private boolean needsReload(Path requestedFile) {
        if (serverData.isEmpty() || !requestedFile.equals(serverDataFile)) {
            return true;
        }
        if (expiry.isExpired()) {
            LOGGER.info("Report data cache is older than {} seconds, re-reading file `{}`", expiry.ttlSeconds(), requestedFile);
            return true;
        }
        return false;
    }
}
