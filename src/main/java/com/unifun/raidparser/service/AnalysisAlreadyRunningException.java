package com.unifun.raidparser.service;

/**
 * Анализ уже идёт. Параллельный запуск не просто дублировал бы работу:
 * он повторно ходил бы по SSH на те же серверы.
 */
public class AnalysisAlreadyRunningException extends RuntimeException {
    public AnalysisAlreadyRunningException(String message) {
        super(message);
    }
}
