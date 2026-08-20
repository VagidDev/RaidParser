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

    private int connectTimeoutMs = 10_000;
    /** Бюджет на неактивность соединения при чтении вывода команды. */
    private int commandTimeoutMs = 60_000;
    /** Путь к known_hosts; задан — включает проверку ключа хоста. */
    private String knownHostsFile;
    /** Отключение проверки ключа хоста оставлено значением по умолчанию ради прокси-хостов. */
    private boolean strictHostKeyChecking = false;
}
