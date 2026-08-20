package com.unifun.raidparser.handlers;

import com.unifun.raidparser.builder.ServerStatusBuilder;
import com.unifun.raidparser.config.CacheConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ServerStatusDataHandler implements ManagedCache {
    private static final Logger LOGGER = LogManager.getLogger(ServerStatusDataHandler.class);
    private static final String CACHE_NAME = "status";

    private final ConcurrentHashMap<String, CachedStatus> serverStatuses = new ConcurrentHashMap<>();
    private final Clock clock;
    private final long ttlSeconds;

    public ServerStatusDataHandler(Clock clock, CacheConfig cacheConfig) {
        this.clock = clock;
        this.ttlSeconds = cacheConfig.getServerStatusTtlSeconds();
    }

    /** Статус вместе со временем последнего обновления: TTL считается по каждому серверу отдельно. */
    private record CachedStatus(ServerStatus status, Instant updatedAt) {
    }

    public void updateAll(List<ServerStatus> serverStatuses) {
        if (serverStatuses == null) {
            return;
        }
        serverStatuses.stream().filter(Objects::nonNull).forEach(this::updateStatus);
    }

    public void updateStatus(ServerStatus newServerStatus) {
        if (newServerStatus == null || newServerStatus.serverName() == null) {
            LOGGER.warn("Skipping status update: server status or its name is null");
            return;
        }
        String serverName = newServerStatus.serverName();
        LOGGER.debug("Updating status for server: {}", serverName);

        serverStatuses.compute(serverName, (key, cached) -> {
            ServerStatus currentServerStatus = liveStatus(cached, serverName);
            ServerStatusBuilder builder = new ServerStatusBuilder().serverName(serverName);

            if (currentServerStatus != null) {
                currentServerStatus.healthStatusMap().forEach(builder::addHealthStatus);
            }

            newServerStatus.healthStatusMap().forEach((type, newResponse) -> {
                AnalyzeResponse<? extends Status> currentResponse = (currentServerStatus != null)
                        ? currentServerStatus.healthStatusMap().get(type)
                        : null;
                if (currentResponse == null) {
                    builder.addHealthStatus(type, newResponse);
                } else if (newResponse.getStatus().getPriority() < currentResponse.getStatus().getPriority()) {
                    LOGGER.info("Server '{}' for [{}] status changed from '{}' to '{}' due to higher priority",
                            serverName, type, currentResponse.getStatus(), newResponse.getStatus());
                    builder.addHealthStatus(type, newResponse);
                }
            });

            return new CachedStatus(builder.build(), clock.instant());
        });
    }

    public void updateStatus(String serverName, HealthType type, AnalyzeResponse<Status> newResponse) {
        LOGGER.debug("Updating status for server: {}, component: {}, new status: {}", serverName, type, newResponse.getStatus());

        serverStatuses.compute(serverName, (key, cached) -> {
            ServerStatus currentServerStatus = liveStatus(cached, serverName);
            ServerStatusBuilder builder = new ServerStatusBuilder().serverName(serverName);

            if (currentServerStatus == null) {
                return new CachedStatus(builder.addHealthStatus(type, newResponse).build(), clock.instant());
            }

            currentServerStatus.healthStatusMap().forEach(builder::addHealthStatus);
            AnalyzeResponse<? extends Status> currentResponse = currentServerStatus.healthStatusMap().get(type);

            if (currentResponse == null) {
                builder.addHealthStatus(type, newResponse);
            } else if (newResponse.getStatus().getPriority() < currentResponse.getStatus().getPriority()) {
                LOGGER.info("Server '{}' [{}] status changed from '{}' to '{}' due to higher priority",
                        serverName, type, currentResponse.getStatus(), newResponse.getStatus());
                builder.addHealthStatus(type, newResponse);
            }

            return new CachedStatus(builder.build(), clock.instant());
        });
    }

    public ServerStatus get(String serverName) {
        CachedStatus cached = serverStatuses.get(serverName);
        if (cached == null) {
            return null;
        }
        if (isExpired(cached)) {
            evict(serverName);
            return null;
        }
        return cached.status();
    }

    public List<ServerStatus> getAll() {
        evictExpired();
        return serverStatuses.values().stream()
                .map(CachedStatus::status)
                .toList();
    }

    public void clear() {
        LOGGER.debug("Clear server status cache");
        serverStatuses.clear();
    }

    @Override
    public String cacheName() {
        return CACHE_NAME;
    }

    @Override
    public CacheState state() {
        Map<String, CachedStatus> snapshot = Map.copyOf(serverStatuses);
        Instant oldest = snapshot.values().stream()
                .map(CachedStatus::updatedAt)
                .min(Comparator.naturalOrder())
                .orElse(null);

        return new CacheState(
                CACHE_NAME,
                !snapshot.isEmpty(),
                snapshot.size(),
                oldest == null ? Duration.ZERO : Duration.between(oldest, clock.instant()),
                ttlSeconds,
                snapshot.values().stream().anyMatch(this::isExpired)
        );
    }

    /**
     * Устаревшая запись считается отсутствующей, иначе давний отказ навсегда
     * перебивал бы новый статус OK: слияние оставляет наиболее серьёзный из двух.
     */
    private ServerStatus liveStatus(CachedStatus cached, String serverName) {
        if (cached == null) {
            return null;
        }
        if (isExpired(cached)) {
            LOGGER.info("Status of server '{}' is older than {} seconds, replacing it instead of merging", serverName, ttlSeconds);
            return null;
        }
        return cached.status();
    }

    private boolean isExpired(CachedStatus cached) {
        if (ttlSeconds <= 0) {
            return false;
        }
        return !clock.instant().isBefore(cached.updatedAt().plusSeconds(ttlSeconds));
    }

    private void evictExpired() {
        if (ttlSeconds <= 0) {
            return;
        }
        List<String> expired = new ArrayList<>();
        serverStatuses.forEach((serverName, cached) -> {
            if (isExpired(cached)) {
                expired.add(serverName);
            }
        });
        expired.forEach(this::evict);
    }

    private void evict(String serverName) {
        if (serverStatuses.remove(serverName) != null) {
            LOGGER.info("Removed outdated status of server '{}' from cache", serverName);
        }
    }
}
