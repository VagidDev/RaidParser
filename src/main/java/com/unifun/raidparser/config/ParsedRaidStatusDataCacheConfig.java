package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("raid.parser.cache")
@Getter @Setter
public class ParsedRaidStatusDataCacheConfig {
    private long driveStatusAgeSeconds = 60;
    private long powerSupplyStatusAgeSeconds = 60;
    private long batteryStatusAgeSeconds = 60;
}
