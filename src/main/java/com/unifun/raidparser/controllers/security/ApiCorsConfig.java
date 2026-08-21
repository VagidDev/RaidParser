package com.unifun.raidparser.controllers.security;

import com.unifun.raidparser.config.ApiConfig;
import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.controllers.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS для внешнего веб-интерфейса. По умолчанию выключен: список origins
 * задаётся в `raid.parser.api.allowed-origins`, звёздочку не подставляем,
 * потому что часть эндпоинтов запускает команды на серверах.
 */
@Configuration
@Profile(Profiles.SERVER)
@RequiredArgsConstructor
public class ApiCorsConfig implements WebMvcConfigurer {
    private static final Logger LOGGER = LogManager.getLogger(ApiCorsConfig.class);

    private final ApiConfig apiConfig;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (apiConfig.getAllowedOrigins().isEmpty()) {
            LOGGER.info("CORS is disabled: `raid.parser.api.allowed-origins` is empty");
            return;
        }

        LOGGER.info("CORS is enabled for origins {}", apiConfig.getAllowedOrigins());
        registry.addMapping(ApiPaths.BASE + "/**")
                .allowedOrigins(apiConfig.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "DELETE")
                .allowedHeaders("*");
    }
}
