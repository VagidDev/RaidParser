package com.unifun.raidparser.console;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsoleDispatcher {
    private final InteractiveConsoleHandler interactiveConsoleHandler;

    public void handle(String[] args) {
        for (String argument : args) {
            switch (argument) {
                case "-i", "--interactive" -> {
                    interactiveConsoleHandler.startInteractiveSession();
                    return;
                } //System.out.println(); //TODO: interactive flow
                case "-p", "--pase-status" -> System.out.println(); //TODO: make flow for parsing into files
                case "-d", "--date" -> System.out.println(); //TODO: specify date
                case "-m", "--manual-check" -> System.out.println(); //TODO: check servers from manual configuration
                case "-h", "--help" -> System.out.println(); //TODO: show usage
            }
        }
    }
}
