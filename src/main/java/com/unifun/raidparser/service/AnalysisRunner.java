package com.unifun.raidparser.service;

import com.unifun.raidparser.dto.ServerStatus;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Запуск анализа с защитой от параллельных прогонов.
 * <p>
 * Прогон занимает минуты и ходит по SSH на боевые серверы, поэтому второй
 * одновременный запуск отклоняется, а не выстраивается в очередь.
 */
@Service
@RequiredArgsConstructor
public class AnalysisRunner {
    private static final Logger LOGGER = LogManager.getLogger(AnalysisRunner.class);

    public static final String MODE_REPORT = "report";
    public static final String MODE_HOSTS = "hosts";
    public static final String MODE_FULL = "full";

    private final RaidParserService raidParserService;
    private final SftpFileService sftpFileService;
    private final Clock clock;

    private final ReentrantLock lock = new ReentrantLock();
    private volatile AnalysisRun lastRun;

    public boolean isRunning() {
        return lock.isLocked();
    }

    public Optional<AnalysisRun> lastRun() {
        return Optional.ofNullable(lastRun);
    }

    /** Разбор отчёта за дату: файл берётся из локального каталога или с sftp. */
    public AnalysisRun runReportAnalysis(LocalDate date) {
        return run(MODE_REPORT, () -> raidParserService.analyzeStatusFromReportFile(reportFile(date)));
    }

    /** Проверка серверов командами по SSH. */
    public AnalysisRun runHostAnalysis() {
        return run(MODE_HOSTS, raidParserService::analyzeStatusFromHosts);
    }

    /** Отчёт и проверка хостов в одном прогоне, статусы объединяются в кэше. */
    public AnalysisRun runFullAnalysis(LocalDate date) {
        return run(MODE_FULL, () -> {
            Path reportFile = reportFile(date);
            List<ServerStatus> statuses = new ArrayList<>(raidParserService.analyzeStatusFromReportFile(reportFile));
            statuses.addAll(raidParserService.analyzeStatusFromHosts());
            return statuses;
        });
    }

    private AnalysisRun run(String mode, Supplier<List<ServerStatus>> analysis) {
        if (!lock.tryLock()) {
            throw new AnalysisAlreadyRunningException("Analysis is already running, try again later");
        }

        try {
            Instant startedAt = clock.instant();
            LOGGER.info("Starting `{}` analysis", mode);

            List<ServerStatus> statuses = analysis.get();

            AnalysisRun run = new AnalysisRun(mode, startedAt, clock.instant(), List.copyOf(statuses));
            lastRun = run;
            LOGGER.info("Finished `{}` analysis: {} server(s) in {} ms", mode, run.servers(), run.durationMs());
            return run;
        } finally {
            lock.unlock();
        }
    }

    private Path reportFile(LocalDate date) {
        Path reportFile = sftpFileService.getFileForDate(date);
        if (reportFile == null) {
            throw new ReportNotFoundException(date);
        }
        return reportFile;
    }
}
