package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties("util.date")
@RequiredArgsConstructor
@Getter
public class DatePatternsConfig {
    private final Map<String, String> formats;
}