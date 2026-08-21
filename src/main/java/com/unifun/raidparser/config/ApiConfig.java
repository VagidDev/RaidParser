package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
    /**
     * Origins, которым разрешены запросы из браузера. Пусто — CORS выключен,
     * и веб-интерфейс с другого адреса работать не сможет.
     */
    private List<String> allowedOrigins = List.of();
}
