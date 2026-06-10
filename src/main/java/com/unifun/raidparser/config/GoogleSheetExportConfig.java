package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("sheets.export")
@Getter @Setter
public class GoogleSheetExportConfig {
    private String spreadsheetId;
    private String diskRange;
    private String psuRange;
    private String batteryRange;
}
