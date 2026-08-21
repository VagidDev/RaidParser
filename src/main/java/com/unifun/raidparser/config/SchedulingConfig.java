package com.unifun.raidparser.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Планировщик поднимается только в серверном режиме и только если он включён. */
@Configuration
@Profile(Profiles.SERVER)
@ConditionalOnProperty(prefix = "raid.parser.schedule", name = "enabled", havingValue = "true")
@EnableScheduling
public class SchedulingConfig {
}
