package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.CacheConfig;
import com.unifun.raidparser.config.HostOverviewLoaderConfig;
import com.unifun.raidparser.loader.HttpClientHostOverviewDataLoader;
import com.unifun.raidparser.util.FileChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class HostOverviewDataHandler implements ManagedCache {
    private static final Logger LOGGER = LogManager.getLogger(HostOverviewDataHandler.class);
    private static final String CACHE_NAME = "hosts-file";

    private final HostOverviewLoaderConfig hostOverviewLoaderConfig;
    private final HttpClientHostOverviewDataLoader httpClientHostOverviewDataLoader;
    private final FileChecker fileChecker;
    private final Clock clock;
    private final long ttlSeconds;

    public HostOverviewDataHandler(HostOverviewLoaderConfig hostOverviewLoaderConfig,
                                   HttpClientHostOverviewDataLoader httpClientHostOverviewDataLoader,
                                   FileChecker fileChecker,
                                   Clock clock,
                                   CacheConfig cacheConfig) {
        this.hostOverviewLoaderConfig = hostOverviewLoaderConfig;
        this.httpClientHostOverviewDataLoader = httpClientHostOverviewDataLoader;
        this.fileChecker = fileChecker;
        this.clock = clock;
        this.ttlSeconds = cacheConfig.getHostOverviewTtlSeconds();
    }

    public String getData() {
        String data = getDataFromCache();
        if (!data.isBlank()) {
            return data;
        }
        LOGGER.info("Getting data from loader");
        data = httpClientHostOverviewDataLoader.loadData();
        writeDataToCache(data);
        return data;
    }

    public String getActualData() {
        clear();
        return getData();
    }

    @Override
    public String cacheName() {
        return CACHE_NAME;
    }

    @Override
    public void clear() {
        Path cacheFile = hostOverviewLoaderConfig.getCacheFilePath();
        try {
            if (Files.deleteIfExists(cacheFile)) {
                LOGGER.info("Cache file `{}` is deleted", cacheFile);
            }
        } catch (IOException e) {
            LOGGER.error("Cannot delete cache file `{}`", cacheFile, e);
        }
    }

    @Override
    public CacheState state() {
        Path cacheFile = hostOverviewLoaderConfig.getCacheFilePath();
        boolean exists = hasContent(cacheFile);
        return new CacheState(
                CACHE_NAME,
                exists,
                exists ? 1 : 0,
                exists ? fileAge(cacheFile) : Duration.ZERO,
                ttlSeconds,
                exists && isOutdated(cacheFile)
        );
    }

    private String getDataFromCache() {
        Path cacheFile = hostOverviewLoaderConfig.getCacheFilePath();
        if (!hasContent(cacheFile)) {
            LOGGER.info("Cache file `{}` is empty or does not exist", cacheFile);
            return "";
        }

        // Возраст берём у самого файла: так кэш истекает и между запусками приложения.
        if (isOutdated(cacheFile)) {
            LOGGER.info("Cache file `{}` is older than {} seconds, ignoring it", cacheFile, ttlSeconds);
            return "";
        }

        try {
            LOGGER.info("Reading data from cache");
            return Files.readString(cacheFile);
        } catch (IOException e) {
            LOGGER.error("Cannot read data from cache file `{}`", cacheFile, e);
            return "";
        }
    }

    private void writeDataToCache(String data) {
        Path cacheFile = hostOverviewLoaderConfig.getCacheFilePath();
        if (!fileChecker.ensureFileExists(cacheFile)) {
            LOGGER.error("Cannot create cache file `{}`! Cannot write data to cache", cacheFile);
            return;
        }

        try {
            LOGGER.info("Writing data to cache");
            Files.writeString(cacheFile, data);
        } catch (IOException e) {
            LOGGER.error("Cannot write data to cache file `{}`", cacheFile, e);
        }
    }

    private boolean hasContent(Path cacheFile) {
        try {
            return Files.isRegularFile(cacheFile) && Files.size(cacheFile) > 0;
        } catch (IOException e) {
            LOGGER.error("Cannot read size of cache file `{}`", cacheFile, e);
            return false;
        }
    }

    private boolean isOutdated(Path cacheFile) {
        if (ttlSeconds <= 0) {
            return false;
        }
        return fileAge(cacheFile).getSeconds() >= ttlSeconds;
    }

    private Duration fileAge(Path cacheFile) {
        try {
            Instant modifiedAt = Files.getLastModifiedTime(cacheFile).toInstant();
            return Duration.between(modifiedAt, clock.instant());
        } catch (IOException e) {
            LOGGER.error("Cannot read modification time of cache file `{}`", cacheFile, e);
            return Duration.ZERO;
        }
    }
}
