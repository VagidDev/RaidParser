package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties("host-overview.loader")
@Getter @Setter
public class HostOverviewLoaderConfig {
    private String login;
    private String password;
    private String authorizationLink;
    private String dataLoaderLink;
    private Path cacheFilePath;
}
