package com.unifun.raidparser.service;

import com.unifun.raidparser.handlers.CacheState;
import com.unifun.raidparser.handlers.ManagedCache;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CacheServiceTest {

    private static class TestCache implements ManagedCache {
        private final String name;
        private int cleared;

        TestCache(String name) {
            this.name = name;
        }

        @Override public String cacheName() { return name; }
        @Override public void clear() { cleared++; }
        @Override public CacheState state() {
            return new CacheState(name, true, 3, Duration.ofSeconds(10), 60, false);
        }
    }

    @Test
    void clearAll_clearsEveryCache() {
        TestCache status = new TestCache("status");
        TestCache hosts = new TestCache("hosts");
        CacheService cacheService = new CacheService(List.of(status, hosts));

        cacheService.clearAll();

        assertEquals(1, status.cleared);
        assertEquals(1, hosts.cleared);
    }

    @Test
    void clear_byName_clearsOnlyRequestedCache() {
        TestCache status = new TestCache("status");
        TestCache hosts = new TestCache("hosts");
        CacheService cacheService = new CacheService(List.of(status, hosts));

        assertTrue(cacheService.clear("STATUS"));

        assertEquals(1, status.cleared);
        assertEquals(0, hosts.cleared);
    }

    @Test
    void clear_unknownName_reportsFailureAndClearsNothing() {
        TestCache status = new TestCache("status");
        CacheService cacheService = new CacheService(List.of(status));

        assertFalse(cacheService.clear("nope"));
        assertEquals(0, status.cleared);
    }

    @Test
    void namesAndStates_areSortedByName() {
        CacheService cacheService = new CacheService(List.of(new TestCache("status"), new TestCache("commands")));

        assertEquals(List.of("commands", "status"), cacheService.cacheNames());
        assertEquals(List.of("commands", "status"), cacheService.states().stream().map(CacheState::name).toList());
    }
}
