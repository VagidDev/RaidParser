package com.unifun.raidparser.console;

import org.springframework.stereotype.Component;

@Component
public class ConsoleDispatcher {
    public void handle(String[] args) {
        for (String argument : args) {
            switch (argument) {
                case "-i", "--interactive" ->  System.out.println(); //TODO: interactive flow
                case "-p", "--pase-status" -> System.out.println(); //TODO: make flow for parsing into files
                case "-d", "--date" -> System.out.println(); //TODO: specify date
                case "-m", "--manual-check" -> System.out.println(); //TODO: check servers from manual configuration
                case "-h", "--help" -> System.out.println(); //TODO: show usage
            }
        }
    }
}
