package com.unifun.raidparser.controllers.dto;

public record CacheStateResponse(
        String name,
        boolean loaded,
        int size,
        long ageSeconds,
        long ttlSeconds,
        boolean expired
) {
}
