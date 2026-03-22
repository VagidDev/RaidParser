package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("raid.parser.output-files")
@Getter @Setter
public class OutputStatusFileConfig {
    private String driveStatus;
    private String psuStatus;
    private String batteryStatus;
}
