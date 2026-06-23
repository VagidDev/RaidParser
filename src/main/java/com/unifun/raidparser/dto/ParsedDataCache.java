package com.unifun.raidparser.dto;

import com.unifun.raidparser.core.filters.Status;
import lombok.Getter;
import lombok.Synchronized;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ParsedDataCache<T extends Status> {
    private static final Logger LOGGER = LogManager.getLogger(ParsedDataCache.class);
    private final long cacheMaxAgeSeconds;

    @Getter
    private List<ServerStatus<T>> serverStatusData;
    private LocalDateTime parsedDateTime;
    private Path parsedFromFile;

    public ParsedDataCache(long cacheMaxAgeSeconds) {
        this.serverStatusData = List.of();
        this.cacheMaxAgeSeconds = cacheMaxAgeSeconds;
    }

    public boolean isDataValid(Path reportFilePath) {
        // чем дальше в лес, if else ... if else
        if (reportFilePath == null) {
            LOGGER.info("Empty stored report file!");
            return false;
        } else if (!reportFilePath.equals(parsedFromFile)) {
            LOGGER.info("Report file is different! Stored report file {}, actual report file {}", parsedFromFile, reportFilePath);
            return false;
        } else if (CollectionUtils.isEmpty(serverStatusData)) {
            LOGGER.info("Cache is empty!");
            return false;
        } else if (parsedDateTime == null || cacheMaxAgeSeconds < ChronoUnit.SECONDS.between(parsedDateTime, LocalDateTime.now())) {
            LOGGER.info("Cache data is expired! Cache were created at {} but the store time (in second) is {}", parsedDateTime, cacheMaxAgeSeconds);
            return false;
        }
        return true;
    }

    public synchronized void store(List<ServerStatus<T>> serverStatusData, Path reportFilePath) {
        if (serverStatusData == null || reportFilePath == null) {
            LOGGER.warn("Cannot write data to cache due to null objects!");
            return;
        }
        this.parsedDateTime = LocalDateTime.now();
        this.serverStatusData = serverStatusData;
        this.parsedFromFile = reportFilePath;
    }

}
