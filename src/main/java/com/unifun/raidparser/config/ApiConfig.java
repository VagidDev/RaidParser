package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("raid.parser.api")
@Getter @Setter
public class ApiConfig {
    /**
     * Ключ доступа к API. Пустое значение — проверка выключена.
     * Стоит задать, если порт виден кому-то кроме доверенной сети:
     * часть эндпоинтов запускает команды на боевых серверах.
     */
    private String key = "";
    private String keyHeader = "X-API-Key";
}
