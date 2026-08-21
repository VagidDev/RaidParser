package com.unifun.raidparser.controllers;

import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.controllers.dto.CacheStateResponse;
import com.unifun.raidparser.controllers.error.NotFoundException;
import com.unifun.raidparser.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Состояние кэшей и их сброс — то же, что команды cache-info и clear-cache в консоли. */
@RestController
@Profile(Profiles.SERVER)
@RequestMapping(ApiPaths.BASE + "/caches")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    @GetMapping
    public List<CacheStateResponse> caches() {
        return cacheService.states().stream()
                .map(state -> new CacheStateResponse(
                        state.name(),
                        state.loaded(),
                        state.size(),
                        state.age().toSeconds(),
                        state.ttlSeconds(),
                        state.expired()))
                .toList();
    }

    @DeleteMapping
    public List<CacheStateResponse> clearAll() {
        cacheService.clearAll();
        return caches();
    }

    @DeleteMapping("/{name}")
    public List<CacheStateResponse> clear(@PathVariable String name) {
        if (!cacheService.clear(name)) {
            throw new NotFoundException("Unknown cache `" + name + "`. Known caches: " + cacheService.cacheNames());
        }
        return caches();
    }
}
