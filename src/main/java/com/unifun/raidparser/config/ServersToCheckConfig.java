package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("health-checker")
@Getter @Setter
public class ServersToCheckConfig {
    private String savingDirectory = "./status";
    private String serversToCheckConfigFile = "./config/servers-to-check.conf";
    private String proxyServerIp = "127.0.0.1";
}
