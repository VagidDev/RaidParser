package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties("util.date")
@Getter
@Setter
//TODO: add constructor binding or add setters
public class DatePatternsConfig {
    private String dateStringFormat;
    private Map<String, String> formats;
}