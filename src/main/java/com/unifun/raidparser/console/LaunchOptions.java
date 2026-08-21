package com.unifun.raidparser.console;

/**
 * Результат разбора аргументов командной строки.
 *
 * @param mode  выбранный режим запуска
 * @param error текст ошибки, если аргументы разобрать не удалось
 */
public record LaunchOptions(LaunchMode mode, String error) {

    public static LaunchOptions of(LaunchMode mode) {
        return new LaunchOptions(mode, null);
    }

    public static LaunchOptions invalid(String error) {
        return new LaunchOptions(LaunchMode.HELP, error);
    }

    public boolean hasError() {
        return error != null;
    }
}
