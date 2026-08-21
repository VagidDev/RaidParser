package com.unifun.raidparser.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleDispatcherTest {

    @Test
    void noArguments_startsInteractiveMode() {
        assertEquals(LaunchMode.INTERACTIVE, ConsoleDispatcher.parse(new String[0]).mode());
        assertEquals(LaunchMode.INTERACTIVE, ConsoleDispatcher.parse(null).mode());
    }

    @Test
    void interactiveFlags_selectInteractiveMode() {
        assertEquals(LaunchMode.INTERACTIVE, ConsoleDispatcher.parse(new String[]{"-i"}).mode());
        assertEquals(LaunchMode.INTERACTIVE, ConsoleDispatcher.parse(new String[]{"--interactive"}).mode());
    }

    @Test
    void daemonFlags_selectServerMode() {
        assertEquals(LaunchMode.SERVER, ConsoleDispatcher.parse(new String[]{"-d"}).mode());
        assertEquals(LaunchMode.SERVER, ConsoleDispatcher.parse(new String[]{"--daemon"}).mode());
    }

    @Test
    void helpFlags_selectHelpMode() {
        assertEquals(LaunchMode.HELP, ConsoleDispatcher.parse(new String[]{"-h"}).mode());
        assertEquals(LaunchMode.HELP, ConsoleDispatcher.parse(new String[]{"--help"}).mode());
    }

    @Test
    void springProperties_arePassedThroughAndDoNotSelectMode() {
        // run.sh передаёт --spring.config.location: такие аргументы адресованы Spring,
        // и принимать их за неизвестную опцию нельзя.
        LaunchOptions options = ConsoleDispatcher.parse(new String[]{"-d", "--server.port=9090", "--spring.config.location=./config.yaml"});

        assertFalse(options.hasError());
        assertEquals(LaunchMode.SERVER, options.mode());
    }

    @Test
    void onlySpringProperties_fallBackToInteractiveMode() {
        LaunchOptions options = ConsoleDispatcher.parse(new String[]{"--spring.config.location=./config.yaml"});

        assertFalse(options.hasError());
        assertEquals(LaunchMode.INTERACTIVE, options.mode());
    }

    @Test
    void unknownOption_isReportedAsError() {
        LaunchOptions options = ConsoleDispatcher.parse(new String[]{"-x"});

        assertTrue(options.hasError());
        assertEquals(LaunchMode.HELP, options.mode());
        assertTrue(options.error().contains("-x"));
    }

    @Test
    void conflictingModes_areReportedAsError() {
        LaunchOptions options = ConsoleDispatcher.parse(new String[]{"-i", "-d"});

        assertTrue(options.hasError());
        assertTrue(options.error().toLowerCase().contains("режим"));
    }

    @Test
    void repeatedSameFlag_isAccepted() {
        assertFalse(ConsoleDispatcher.parse(new String[]{"-d", "--daemon"}).hasError());
    }
}
