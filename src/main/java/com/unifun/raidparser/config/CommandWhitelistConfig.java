package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;

@Configuration
@ConfigurationProperties("util.commands")
@Getter @Setter
public class CommandWhitelistConfig {
    private List<String> whitelist;

    /**
     * Пустой whitelist — это ошибка конфигурации, а не «разрешено всё»:
     * раньше отсутствие ключа давало NPE прямо при создании бина.
     */
    @Bean
    public List<Pattern> commandPatterns() {
        if (CollectionUtils.isEmpty(whitelist)) {
            throw new IllegalStateException("`util.commands.whitelist` is empty — no command would be allowed to run");
        }
        return whitelist.stream().map(Pattern::compile).toList();
    }
}
