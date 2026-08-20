package com.unifun.raidparser.util;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Отметка времени последней загрузки кэша и проверка его срока жизни.
 * TTL <= 0 отключает истечение по времени: такой кэш живёт до ручного сброса.
 */
public class CacheExpiry {
    private final Clock clock;
    private final long ttlSeconds;

    private volatile Instant loadedAt;

    public CacheExpiry(Clock clock, long ttlSeconds) {
        this.clock = clock;
        this.ttlSeconds = ttlSeconds;
    }

    public void markLoaded() {
        this.loadedAt = clock.instant();
    }

    public void invalidate() {
        this.loadedAt = null;
    }

    public boolean isLoaded() {
        return loadedAt != null;
    }

    public boolean hasTtl() {
        return ttlSeconds > 0;
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }

    /** Незагруженный кэш не считается устаревшим — он просто пустой. */
    public boolean isExpired() {
        Instant loaded = loadedAt;
        if (loaded == null || !hasTtl()) {
            return false;
        }
        return !clock.instant().isBefore(loaded.plusSeconds(ttlSeconds));
    }

    public Duration age() {
        Instant loaded = loadedAt;
        return loaded == null ? Duration.ZERO : Duration.between(loaded, clock.instant());
    }
}
