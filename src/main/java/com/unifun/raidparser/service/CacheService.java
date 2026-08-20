package com.unifun.raidparser.service;

import com.unifun.raidparser.handlers.CacheState;
import com.unifun.raidparser.handlers.ManagedCache;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Единая точка управления кэшами. Реализации {@link ManagedCache} собираются
 * контейнером, поэтому новый кэш достаточно объявить бином — руками
 * дописывать его здесь и в консоли не нужно.
 */
@Service
@RequiredArgsConstructor
public class CacheService {
    private static final Logger LOGGER = LogManager.getLogger(CacheService.class);

    private final List<ManagedCache> caches;

    public List<String> cacheNames() {
        return caches.stream().map(ManagedCache::cacheName).sorted().toList();
    }

    public List<CacheState> states() {
        return caches.stream()
                .map(ManagedCache::state)
                .sorted(Comparator.comparing(CacheState::name))
                .toList();
    }

    public void clearAll() {
        LOGGER.info("Clearing all caches: {}", cacheNames());
        caches.forEach(ManagedCache::clear);
    }

    /**
     * @return false, если кэша с таким именем нет — вызывающий код может
     *         показать список доступных имён вместо молчаливого «готово».
     */
    public boolean clear(String cacheName) {
        Optional<ManagedCache> cache = caches.stream()
                .filter(managedCache -> managedCache.cacheName().equalsIgnoreCase(cacheName))
                .findFirst();

        if (cache.isEmpty()) {
            LOGGER.warn("Unknown cache `{}`. Known caches: {}", cacheName, cacheNames());
            return false;
        }

        LOGGER.info("Clearing cache `{}`", cacheName);
        cache.get().clear();
        return true;
    }
}
