package com.unifun.raidparser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {
    /** Отдельный бин, чтобы возраст кэшей можно было проверять в тестах фиксированным временем. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
