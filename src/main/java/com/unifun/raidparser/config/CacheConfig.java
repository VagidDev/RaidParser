package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Время жизни кэшей пакета handlers. Значение <= 0 означает,
 * что кэш по времени не истекает и сбрасывается только вручную.
 */
@Configuration
@ConfigurationProperties("raid.parser.cache")
@Getter @Setter
public class CacheConfig {
    /** Проанализированные статусы серверов. */
    private long serverStatusTtlSeconds = 3600;
    /** Данные, разобранные из файла отчёта. */
    private long reportDataTtlSeconds = 3600;
    /** Список серверов из HostOverview: и файл с сырым ответом, и разобранный список. */
    private long hostOverviewTtlSeconds = 86_400;
    /** Команды из servers-to-check файла. */
    private long serversToCheckTtlSeconds = 300;
}
