package com.unifun.raidparser.console;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConsoleDispatcher {
    private static final String USAGE = """
            Raid Parser %s

            Использование: java -jar raid_parser.jar [опция]

              -i, --interactive    интерактивный режим (по умолчанию)
              -h, --help           показать эту справку

            Ещё не реализовано:
              -p, --parse-status   разбор отчёта сразу в статус-файлы
              -d, --date           обработать отчёт за указанную дату
              -m, --manual-check   проверить серверы из ручной конфигурации
            """;

    private static final String VERSION = "v5.0";

    private final InteractiveConsoleHandler interactiveConsoleHandler;

    public void handle(String[] args) {
        if (args == null || args.length == 0) {
            interactiveConsoleHandler.startInteractiveSession();
            return;
        }

        for (String argument : args) {
            switch (argument) {
                case "-i", "--interactive" -> {
                    interactiveConsoleHandler.startInteractiveSession();
                    return;
                }
                case "-h", "--help" -> {
                    printUsage();
                    return;
                }
                case "-p", "--parse-status", "-d", "--date", "-m", "--manual-check" -> {
                    System.out.printf("Опция %s ещё не реализована.%n", argument);
                    printUsage();
                    return;
                }
                default -> {
                    System.err.printf("Неизвестная опция: %s%n", argument);
                    printUsage();
                    return;
                }
            }
        }
    }

    private void printUsage() {
        System.out.printf(USAGE, VERSION);
    }
}
