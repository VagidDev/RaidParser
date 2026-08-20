package com.unifun.raidparser.validators.command;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Qualifier("configCommandValidator")
public class ConfigCommandValidator implements CommandValidator{
    private static final Logger LOGGER = LogManager.getLogger(ConfigCommandValidator.class);
    private final List<Pattern> whiteList;

    @Override
    public Boolean validate(String command) {
        if (command == null || command.isBlank()) {
            return false;
        }
        boolean allowed = whiteList.stream()
                .anyMatch(pattern -> pattern.matcher(command).matches());

        if (!allowed) {
            LOGGER.warn("Command `{}` is not allowed!", command);
            return false;
        }

        return true;
    }
}
