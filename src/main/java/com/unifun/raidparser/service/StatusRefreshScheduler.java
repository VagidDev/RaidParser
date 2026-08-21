package com.unifun.raidparser.service;

import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.config.ScheduleConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Периодическое обновление статусов в серверном режиме.
 * Прогон идёт через {@link AnalysisRunner}, поэтому не пересекается
 * с запуском из API: если прогон уже идёт, задача просто пропускается.
 */
@Service
@Profile(Profiles.SERVER)
@ConditionalOnProperty(prefix = "raid.parser.schedule", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class StatusRefreshScheduler {
    private static final Logger LOGGER = LogManager.getLogger(StatusRefreshScheduler.class);

    private final AnalysisRunner analysisRunner;
    private final ScheduleConfig scheduleConfig;
    private final Clock clock;

    @Scheduled(cron = "${raid.parser.schedule.cron}")
    public void refreshStatuses() {
        String mode = scheduleConfig.getMode();
        try {
            AnalysisRun run = switch (mode) {
                case AnalysisRunner.MODE_REPORT -> analysisRunner.runReportAnalysis(LocalDate.now(clock));
                case AnalysisRunner.MODE_HOSTS -> analysisRunner.runHostAnalysis();
                case AnalysisRunner.MODE_FULL -> analysisRunner.runFullAnalysis(LocalDate.now(clock));
                default -> throw new IllegalStateException(
                        "Unknown `raid.parser.schedule.mode` -> `" + mode + "`, expected one of: report, hosts, full");
            };
            LOGGER.info("Scheduled `{}` analysis finished: {} server(s) in {} ms", mode, run.servers(), run.durationMs());
        } catch (AnalysisAlreadyRunningException e) {
            LOGGER.info("Skipping scheduled `{}` analysis: another analysis is running", mode);
        } catch (Exception e) {
            // Исключение из @Scheduled метода отменило бы дальнейшие запуски задачи.
            LOGGER.error("Scheduled `{}` analysis failed: {}", mode, e.getMessage(), e);
        }
    }
}
