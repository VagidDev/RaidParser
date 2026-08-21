package com.unifun.raidparser.controllers.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unifun.raidparser.config.ApiConfig;
import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.controllers.ApiPaths;
import com.unifun.raidparser.controllers.error.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;

/**
 * Проверка ключа в заголовке. Пока `raid.parser.api.key` пуст, фильтр
 * пропускает всё — это осознанный дефолт для закрытой сети.
 */
@Component
@Profile(Profiles.SERVER)
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LogManager.getLogger(ApiKeyFilter.class);

    private final ApiConfig apiConfig;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // OPTIONS — это preflight браузера, он приходит без наших заголовков
        return HttpMethod.OPTIONS.matches(request.getMethod())
                || !request.getRequestURI().startsWith(ApiPaths.BASE)
                || !StringUtils.hasText(apiConfig.getKey());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String providedKey = request.getHeader(apiConfig.getKeyHeader());

        if (!apiConfig.getKey().equals(providedKey)) {
            LOGGER.warn("Rejected request {} {} due to missing or wrong API key", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(
                    response.getWriter(),
                    new ApiError("unauthorized", "Valid " + apiConfig.getKeyHeader() + " header is required", clock.instant()));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
