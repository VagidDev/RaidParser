package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("sftp.remote.user")
@Getter @Setter
public class SftpUserConfig {
    private String host;
    private int port;
    private String login;
    private String password;
}
