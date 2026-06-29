package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("raid.parser.report-file.bounds-pattern")
@Getter @Setter
public class ReportFileDataBoundsPatternConfig {
    private String driveStart;
    private String driveEnd;
    private String psuStart;
    private String psuEnd;
    private String batteryStart;
    private String batteryEnd;
}
