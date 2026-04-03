package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.HostOverviewLoaderConfig;
import com.unifun.raidparser.loader.HostOverviewDataLoader;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class HostOverviewDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(HostOverviewDataHandler.class);
    private final HostOverviewLoaderConfig hostOverviewLoaderConfig;
    private final HostOverviewDataLoader hostOverviewDataLoader;

    private boolean ensureCacheFileExists() {
        Path cacheFile = hostOverviewLoaderConfig.getCacheFilePath();
        if (Files.exists(cacheFile)) {
            LOGGER.info("Cache file exists!");
            return true;
        }
        LOGGER.warn("Cache file does not exists! Creating cache file {}", cacheFile);
        try {
            Files.createDirectories(cacheFile.getParent());
            Files.createFile(cacheFile);
            LOGGER.info("Cache file {} is created!", cacheFile);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while creating cache file. Error -> {}", e.getMessage(), e);
            return false;
        }
    }

    private String getDataFromCache() {
        if (!ensureCacheFileExists()) {
            LOGGER.error("Cache file does not exists! Cannot get data from cache");
            return "";
        }
        try {
            LOGGER.info("Reading data from cache");
            return Files.readString(hostOverviewLoaderConfig.getCacheFilePath());
        } catch (IOException e) {
            LOGGER.error("Cannot read data from cache file `{}`", hostOverviewLoaderConfig.getCacheFilePath(), e);
            return "";
        }
    }

    private void writeDataToCache(String data) {
        if (!ensureCacheFileExists()) {
            LOGGER.error("Cache file does not exists! Cannot write data to cache");
            return;
        }

        try {
            LOGGER.info("Writing data to cache");
            Files.writeString(hostOverviewLoaderConfig.getCacheFilePath(), data);
        } catch (IOException e) {
            LOGGER.error("Cannot write data to cache file `{}`", hostOverviewLoaderConfig.getCacheFilePath(), e);
        }
    }

    private void clearCache() {
        try {
            LOGGER.info("Deleting cache file...");
            Files.deleteIfExists(hostOverviewLoaderConfig.getCacheFilePath());
        } catch (IOException e) {
            LOGGER.error("Cannot delete cache file `{}`", hostOverviewLoaderConfig.getCacheFilePath(), e);
        }
    }

    public String getActualData() {
        clearCache();
        return getData();
    }

    public String getData() {
        String data = getDataFromCache();
        if (!data.isBlank()) {
            return data;
        }
        LOGGER.info("Getting data from loader");
        data = hostOverviewDataLoader.loadData();
        writeDataToCache(data);
        return data;
    }

}
