package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ssh.user")
@Getter @Setter
public class SshUserConfig {
    private String login;
    private String password;
    private String privateKey;
}
