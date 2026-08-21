package com.unifun.raidparser.controllers;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.unifun.raidparser.controllers.converter.StringToHealthTypeConverter;
import com.unifun.raidparser.controllers.converter.StringToSeverityConverter;
import com.unifun.raidparser.controllers.dto.ComponentStatusResponse;
import com.unifun.raidparser.controllers.dto.ServerStatusResponse;
import com.unifun.raidparser.controllers.error.ApiExceptionHandler;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.component.Severity;
import com.unifun.raidparser.service.StatusQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StatusControllerTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-21T10:00:00Z"), ZoneOffset.UTC);

    private final StatusQueryService statusQueryService = mock(StatusQueryService.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Конвертер настроен так же, как его настраивает Spring Boot,
        // иначе Instant в тесте сериализовался бы иначе, чем в приложении.
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(
                JsonMapper.builder()
                        .addModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .build());

        // Те же конвертеры, что регистрируются в приложении как бины
        FormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new StringToHealthTypeConverter());
        conversionService.addConverter(new StringToSeverityConverter());

        mockMvc = MockMvcBuilders
                .standaloneSetup(new StatusController(statusQueryService, CLOCK))
                .setControllerAdvice(new ApiExceptionHandler(CLOCK))
                .setMessageConverters(converter)
                .setConversionService(conversionService)
                .build();
    }

    private ServerStatusResponse server(String name, Severity severity) {
        return new ServerStatusResponse(name, severity, Map.of(
                HealthType.DRIVE_HEALTH, new ComponentStatusResponse("OK(Failed)", severity, 1, "physicaldrive 1 failed")
        ));
    }

    @Test
    void status_returnsServersWithGeneratedAt() throws Exception {
        when(statusQueryService.query(isNull(), any(), isNull()))
                .thenReturn(List.of(server("host-01", Severity.CRITICAL)));

        mockMvc.perform(get("/api/v1/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedAt").value("2026-08-21T10:00:00Z"))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.servers[0].server").value("host-01"))
                .andExpect(jsonPath("$.servers[0].severity").value("CRITICAL"))
                .andExpect(jsonPath("$.servers[0].components.drive_health.status").value("OK(Failed)"))
                .andExpect(jsonPath("$.servers[0].components.drive_health.details").value("physicaldrive 1 failed"));
    }

    @Test
    void status_passesFiltersToService() throws Exception {
        when(statusQueryService.query(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/status")
                        .param("component", "psu_health")
                        .param("severity", "CRITICAL", "WARNING")
                        .param("server", "db"))
                .andExpect(status().isOk());

        verify(statusQueryService).query(
                eq(HealthType.PSU_HEALTH),
                eq(java.util.Set.of(Severity.CRITICAL, Severity.WARNING)),
                eq("db"));
    }

    @Test
    void status_acceptsShortComponentName() throws Exception {
        when(statusQueryService.query(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/status").param("component", "psu"))
                .andExpect(status().isOk());

        verify(statusQueryService).query(eq(HealthType.PSU_HEALTH), any(), isNull());
    }

    @Test
    void status_unknownComponent_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/status").param("component", "nosuch"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"))
                // в сообщении должны быть перечислены допустимые значения
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("drive_health")));
    }

    @Test
    void serverStatus_unknownServer_returns404() throws Exception {
        when(statusQueryService.findByServerName("nope")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/status/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"))
                .andExpect(jsonPath("$.timestamp").value("2026-08-21T10:00:00Z"));
    }

    @Test
    void serverStatus_knownServer_returnsIt() throws Exception {
        when(statusQueryService.findByServerName("host-01")).thenReturn(Optional.of(server("host-01", Severity.OK)));

        mockMvc.perform(get("/api/v1/status/host-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server").value("host-01"))
                .andExpect(jsonPath("$.severity").value("OK"));
    }
}
