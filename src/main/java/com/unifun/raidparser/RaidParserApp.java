package com.unifun.raidparser;

import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.console.ConsoleDispatcher;
import com.unifun.raidparser.console.InteractiveConsoleHandler;
import com.unifun.raidparser.console.LaunchOptions;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@SpringBootApplication
@ComponentScan(
        basePackages = "com.unifun.raidparser",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
public class RaidParserApp {
    private static final int INVALID_ARGUMENTS_EXIT_CODE = 2;

    public static void main(String[] args) {
        LaunchOptions options = ConsoleDispatcher.parse(args);

        if (options.hasError()) {
            System.err.println(options.error());
            ConsoleDispatcher.printUsage();
            System.exit(INVALID_ARGUMENTS_EXIT_CODE);
        }

        switch (options.mode()) {
            case HELP -> ConsoleDispatcher.printUsage();
            case INTERACTIVE -> runInteractiveSession(args);
            case SERVER -> runServer(args);
        }
    }

    /**
     * Консоли не нужен веб-контейнер, поэтому явно отключаем его:
     * с spring-boot-starter-web в classpath режим по умолчанию был бы SERVLET.
     * Контекст закрываем по выходу из сессии — так срабатывают @PreDestroy.
     */
    private static void runInteractiveSession(String[] args) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(RaidParserApp.class)
                .web(WebApplicationType.NONE)
                .profiles(Profiles.CONSOLE)
                .run(args)) {
            context.getBean(InteractiveConsoleHandler.class).startInteractiveSession();
        }
    }

    /** Серверный режим живёт до остановки процесса: контекст держит контейнер. */
    private static void runServer(String[] args) {
        new SpringApplicationBuilder(RaidParserApp.class)
                .web(WebApplicationType.SERVLET)
                .profiles(Profiles.SERVER)
                .run(args);
    }
}
