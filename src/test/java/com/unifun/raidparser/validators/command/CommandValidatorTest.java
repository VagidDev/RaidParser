package com.unifun.raidparser.validators.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CommandValidatorTest {

    private CommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConfigCommandValidator(Stream.of(
                "^cat /proc/mdstat$",
                "^mdadm --detail$",
                "^mdadm --detail --scan$",
                "^megacli -ldinfo -lall -aall$",
                "^megacli -pdlist -aall$",
                "^storcli /c\\d+ show$",
                "^storcli /c\\d+/vall show$",
                "^storcli /call/vall show$",
                "^hpssacli ctrl all show config$",
                "^ssacli ctrl all show config$",
                "^perccli /c\\d+ show$",
                "^perccli /call/vall show$",
                "^lsblk$",
                "^lsblk -o NAME,SIZE,TYPE,MOUNTPOINT$",
                "^df -h$",
                "^free -m$",
                "^uptime$",
                "^ps aux$"

        ).map(Pattern::compile).toList());
    }

    @Test
    void isValid_shouldReturnTrue_whenCommandIsAllowed() {
        assertTrue(validator.validate("cat /proc/mdstat"));
        assertTrue(validator.validate("mdadm --detail"));
        assertTrue(validator.validate("mdadm --detail --scan"));
        assertTrue(validator.validate("storcli /c0 show"));
        assertTrue(validator.validate("storcli /c1/vall show"));
    }

    @Test
    void isValid_shouldReturnFalse_whenCommandIsNotAllowed() {
        assertFalse(validator.validate("rm -rf /"));
        assertFalse(validator.validate("echo hello"));
        assertFalse(validator.validate("mdadm --create"));
    }

    @Test
    void isValid_shouldReturnFalse_whenCommandIsEmptyOrNull() {
        assertFalse(validator.validate(""));
        assertFalse(validator.validate("   "));
        assertFalse(validator.validate(null));
    }

    @Test
    void isValid_shouldBeStrictMatch_notStartsWith() {
        assertFalse(validator.validate("cat /proc/mdstat && rm -rf /"));
    }

    @Test
    void isValid_shouldHandleDifferentControllers() {
        assertTrue(validator.validate("storcli /c0 show"));
        assertTrue(validator.validate("storcli /c5 show"));
        assertTrue(validator.validate("storcli /c10/vall show"));
    }

    @Test
    void isValid_shouldReturnFalse_whenCommandPartiallyMatches() {
        assertFalse(validator.validate("storcli show"));
        assertFalse(validator.validate("mdadm detail"));
    }
}