package com.unifun.raidparser.handlers;

import java.time.Duration;

/**
 * Снимок состояния кэша для отчёта в консоли.
 *
 * @param name      имя, по которому кэш сбрасывается вручную
 * @param loaded    в кэше есть данные
 * @param size      количество записей
 * @param age       сколько прошло с момента загрузки
 * @param ttlSeconds настроенный TTL, 0 или меньше — истечения по времени нет
 * @param expired   данные устарели и будут перечитаны при следующем обращении
 */
public record CacheState(
        String name,
        boolean loaded,
        int size,
        Duration age,
        long ttlSeconds,
        boolean expired
) {
    public String describe() {
        if (!loaded) {
            return String.format("%-11s пусто%s", name, ttlDescription());
        }
        return String.format("%-11s записей: %-4d возраст: %-9s%s%s",
                name, size, formatAge(), ttlDescription(), expired ? "  [устарел, будет перечитан]" : "");
    }

    private String ttlDescription() {
        return ttlSeconds > 0 ? "  TTL: " + ttlSeconds + "s" : "  TTL: не задан";
    }

    private String formatAge() {
        long totalSeconds = age.toSeconds();
        return String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
    }
}
