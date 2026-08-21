package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Автоматическое обновление статусов в серверном режиме. */
@Configuration
@ConfigurationProperties("raid.parser.schedule")
@Getter @Setter
public class ScheduleConfig {
    /**
     * По умолчанию выключено: планировщик ходит по SSH на боевые серверы,
     * это стоит включать осознанно.
     */
    private boolean enabled = false;
    /** Cron в формате Spring (6 полей). По умолчанию — в начале каждого часа. */
    private String cron = "0 0 * * * *";
    /** Что запускать: report, hosts или full. */
    private String mode = "full";
}
