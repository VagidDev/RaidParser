package com.unifun.raidparser;

import com.unifun.raidparser.console.ConsoleDispatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RaidParserApp {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(RaidParserApp.class, args);
        ConsoleDispatcher consoleDispatcher = context.getBean(ConsoleDispatcher.class);
        consoleDispatcher.handle(args);
    }
}
