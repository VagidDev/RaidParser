package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("health-checker")
@Getter @Setter
public class ServersToCheckConfig {
    private String savingDirectory;
    private String serversToCheckConfigFile;
    private String proxyServerIp;
}
