package com.unifun.raidparser.util;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class CacheExpiryTest {
    private static final Instant START = Instant.parse("2026-08-20T12:00:00Z");

    /** Управляемые часы: тест двигает время вперёд вместо ожидания. */
    private static class TestClock extends Clock {
        private Instant now = START;

        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }

        void advance(Duration duration) { now = now.plus(duration); }
    }

    @Test
    void notLoadedCache_isNotExpired() {
        CacheExpiry expiry = new CacheExpiry(new TestClock(), 60);

        assertFalse(expiry.isLoaded());
        assertFalse(expiry.isExpired());
        assertEquals(Duration.ZERO, expiry.age());
    }

    @Test
    void expiresAfterTtl() {
        TestClock clock = new TestClock();
        CacheExpiry expiry = new CacheExpiry(clock, 60);
        expiry.markLoaded();

        clock.advance(Duration.ofSeconds(59));
        assertFalse(expiry.isExpired());

        clock.advance(Duration.ofSeconds(1));
        assertTrue(expiry.isExpired());
    }

    @Test
    void nonPositiveTtl_neverExpires() {
        TestClock clock = new TestClock();
        CacheExpiry expiry = new CacheExpiry(clock, 0);
        expiry.markLoaded();

        clock.advance(Duration.ofDays(365));

        assertFalse(expiry.hasTtl());
        assertFalse(expiry.isExpired());
    }

    @Test
    void invalidate_resetsLoadedState() {
        TestClock clock = new TestClock();
        CacheExpiry expiry = new CacheExpiry(clock, 60);
        expiry.markLoaded();
        clock.advance(Duration.ofSeconds(120));

        expiry.invalidate();

        assertFalse(expiry.isLoaded());
        assertFalse(expiry.isExpired());
    }

    @Test
    void age_growsWithTime() {
        TestClock clock = new TestClock();
        CacheExpiry expiry = new CacheExpiry(clock, 60);
        expiry.markLoaded();

        clock.advance(Duration.ofSeconds(30));

        assertEquals(Duration.ofSeconds(30), expiry.age());
    }
}
