package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("raid.parser.report-file.bounds-pattern")
@Getter @Setter
public class ReportFileDataBoundsPatternConfig {
    private String driveStart = "=========================drive================================";
    private String driveEnd = "==========================RAM=================================";
    private String psuStart = "==========================PSU=================================";
    private String psuEnd = "=========================DIMM=================================";
    private String batteryStart = "=========================config===============================";
    private String batteryEnd = "=========================drive================================";
}
