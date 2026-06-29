package com.unifun.raidparser.service;

import com.unifun.raidparser.validators.command.CommandValidator;
import com.unifun.raidparser.validators.command.RaidCommandValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommandValidatorService {
    private final List<CommandValidator> commandValidators = List.of(
            new RaidCommandValidator()
    );

    public boolean isValid(String command) {
        for (CommandValidator validator : commandValidators) {
            if (validator.validate(command) == false) {
                return false;
            }
        }
        return true;
    }
}
