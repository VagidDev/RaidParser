package com.unifun.raidparser.service;

import com.unifun.raidparser.validators.command.CommandValidator;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommandValidatorService {
    private static final Logger LOGGER = LogManager.getLogger(CommandValidatorService.class);

    private final List<CommandValidator> commandValidators;

    public CommandValidatorService(List<CommandValidator> commandValidators) {
        this.commandValidators = commandValidators;
    }

    /**
     * Пустой список валидаторов означал бы, что isValid() пропускает любую команду,
     * и «проверок нет» было бы неотличимо от «все проверки прошли».
     */
    @PostConstruct
    void checkValidators() {
        if (commandValidators.isEmpty()) {
            throw new IllegalStateException("No command validators registered — commands would be executed unchecked");
        }
        LOGGER.info("Command validation is done by {} validator(s)", commandValidators.size());
    }

    public boolean isValid(String command) {
        for (CommandValidator validator : commandValidators) {
            if (!validator.validate(command)) {
                return false;
            }
        }
        return true;
    }
}
