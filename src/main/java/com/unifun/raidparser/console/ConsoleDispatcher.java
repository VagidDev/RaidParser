package com.unifun.raidparser.console;

/**
 * Разбор аргументов командной строки и выбор режима запуска.
 * <p>
 * Это не бин: режим нужно знать <b>до</b> создания контекста, иначе им нельзя
 * управлять составом бинов — интерактивному режиму не нужен ни веб-контейнер,
 * ни контроллеры, а серверному не нужна интерактивная консоль.
 */
public final class ConsoleDispatcher {
    private static final String USAGE = """
            Raid Parser %s — состояние дисков, блоков питания и батарей серверов

            Использование: java -jar raid_parser.jar [режим] [--свойство=значение ...]

              -i, --interactive    интерактивная консоль (по умолчанию)
              -d, --daemon         серверный режим: REST API под /api/v1
              -h, --help           показать эту справку

            Свойства Spring передаются как есть, например:
              java -jar raid_parser.jar -d --server.port=9090
              java -jar raid_parser.jar -i --spring.config.location=./config.yaml
            """;

    private static final String VERSION = "v5.0";

    private ConsoleDispatcher() {
    }

    public static LaunchOptions parse(String[] args) {
        if (args == null || args.length == 0) {
            return LaunchOptions.of(LaunchMode.INTERACTIVE);
        }

        LaunchOptions selected = null;
        for (String argument : args) {
            if (isSpringProperty(argument)) {
                continue;
            }

            LaunchMode mode = toMode(argument);
            if (mode == null) {
                return LaunchOptions.invalid("Неизвестная опция: " + argument);
            }
            if (selected != null && selected.mode() != mode) {
                return LaunchOptions.invalid("Указано больше одного режима запуска: " + argument);
            }
            selected = LaunchOptions.of(mode);
        }

        return selected == null ? LaunchOptions.of(LaunchMode.INTERACTIVE) : selected;
    }

    public static void printUsage() {
        System.out.printf(USAGE, VERSION);
    }

    /** Свойства вида --server.port=9090 адресованы Spring, а не нам. */
    private static boolean isSpringProperty(String argument) {
        return argument.startsWith("--") && argument.contains("=");
    }

    private static LaunchMode toMode(String argument) {
        return switch (argument) {
            case "-i", "--interactive" -> LaunchMode.INTERACTIVE;
            case "-d", "--daemon" -> LaunchMode.SERVER;
            case "-h", "--help" -> LaunchMode.HELP;
            default -> null;
        };
    }
}
