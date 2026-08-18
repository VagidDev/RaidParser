package com.unifun.raidparser;

import com.unifun.raidparser.console.ConsoleDispatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.unifun.raidparser",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class // <-- Включает генерацию имён с учётом пакетов
)
public class RaidParserApp {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(RaidParserApp.class, args);
        ConsoleDispatcher consoleDispatcher = context.getBean(ConsoleDispatcher.class);
        consoleDispatcher.handle(args);
    }
}
