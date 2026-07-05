package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties("util.date")
@Getter @Setter
public class DatePatternsConfig {
    private String dateStringFormat = "yyyy_MM_dd";
    private Map<String, String> formats = Map.of(
            "dd.MM.yyyy", "^(0[1-9]|[1-2][0-9]|3[01])\\.(0[1-9]|1[0-2])\\.\\d{4}$",
            "dd-MM-yyyy", "^(0[1-9]|[1-2][0-9]|3[01])-(0[1-9]|1[0-2])-\\d{4}$",
            "yyyy-MM-dd", "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[01])$",
            "MM/dd/yyyy", "^(0[1-9]|1[0-2])/(0[1-9]|[1-2][0-9]|3[01])/\\d{4}$"
    );
}