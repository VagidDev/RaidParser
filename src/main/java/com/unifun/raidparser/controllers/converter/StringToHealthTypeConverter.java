package com.unifun.raidparser.controllers.converter;

import com.unifun.raidparser.core.component.HealthType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

/**
 * Конвертер параметра запроса в {@link HealthType}.
 * <p>
 * Аннотации Jackson на значения enum на биндинг query-параметров не влияют,
 * поэтому имя `psu_health` — то же, что в JSON-ответах и в конфиге —
 * нужно разбирать явно. Принимаются `PSU_HEALTH`, `psu_health` и `psu`.
 */
@Component
public class StringToHealthTypeConverter implements Converter<String, HealthType> {

    @Override
    public HealthType convert(String source) {
        String normalized = source.trim().toUpperCase(Locale.ROOT).replace('-', '_');

        for (HealthType healthType : HealthType.reportable()) {
            if (healthType.name().equals(normalized) || shortNameOf(healthType).equals(normalized)) {
                return healthType;
            }
        }

        throw new IllegalArgumentException("Unknown component `" + source + "`. Expected one of: "
                + Arrays.toString(HealthType.reportable().stream().map(type -> type.name().toLowerCase(Locale.ROOT)).toArray()));
    }

    private String shortNameOf(HealthType healthType) {
        return healthType.name().replace("_HEALTH", "");
    }
}
