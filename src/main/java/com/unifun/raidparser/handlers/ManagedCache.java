package com.unifun.raidparser.handlers;

/**
 * Кэш, который можно сбросить вручную и о состоянии которого можно спросить.
 * Реализации подхватываются {@link com.unifun.raidparser.service.CacheService}
 * автоматически, поэтому новый кэш достаточно объявить бином.
 */
public interface ManagedCache {
    /** Короткое имя для команды `clear-cache <имя>`. */
    String cacheName();

    void clear();

    CacheState state();
}
