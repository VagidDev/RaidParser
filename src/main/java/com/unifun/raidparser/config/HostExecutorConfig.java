package com.unifun.raidparser.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("ssh.parallel")
@Validated
@Getter @Setter
public class HostExecutorConfig {
    /**
     * Значение по умолчанию задано осознанно: с threads = 0
     * Executors.newFixedThreadPool падал ещё на старте контекста.
     */
    @Min(1)
    private int threads = 8;

    /** Бюджет на выполнение всех команд одного хоста. */
    @Min(1)
    private int taskTimeoutSeconds = 300;
}
