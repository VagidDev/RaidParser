package com.unifun.raidparser.service;

import com.unifun.raidparser.validators.command.CommandValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommandValidatorService {
    private final List<CommandValidator> commandValidators;

    public CommandValidatorService(@Qualifier("configCommandValidator") List<CommandValidator> commandValidators) {
        this.commandValidators = commandValidators;
    }

    public boolean isValid(String command) {
        for (CommandValidator validator : commandValidators) {
            if (validator.validate(command) == false) {
                return false;
            }
        }
        return true;
    }
}
