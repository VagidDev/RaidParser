package com.unifun.raidparser.controllers;

import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.controllers.dto.AnalysisResponse;
import com.unifun.raidparser.controllers.dto.AnalysisStateResponse;
import com.unifun.raidparser.mapper.ApiStatusMapper;
import com.unifun.raidparser.service.AnalysisRun;
import com.unifun.raidparser.service.AnalysisRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Запуск анализа. Работает синхронно: ответ приходит с готовым результатом.
 * Пока прогон идёт, повторный запрос получает 409.
 */
@RestController
@Profile(Profiles.SERVER)
@RequestMapping(ApiPaths.BASE + "/analyze")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisRunner analysisRunner;
    private final ApiStatusMapper apiStatusMapper;
    private final Clock clock;

    /** Разбор отчёта за дату; без параметра — за сегодня. */
    @PostMapping("/report")
    public AnalysisResponse analyzeReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return toResponse(analysisRunner.runReportAnalysis(dateOrToday(date)));
    }

    /** Проверка серверов командами по SSH. Занимает минуты. */
    @PostMapping("/hosts")
    public AnalysisResponse analyzeHosts() {
        return toResponse(analysisRunner.runHostAnalysis());
    }

    /** Отчёт и проверка хостов в одном прогоне. */
    @PostMapping("/full")
    public AnalysisResponse analyzeFull(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return toResponse(analysisRunner.runFullAnalysis(dateOrToday(date)));
    }

    /** Идёт ли прогон и чем закончился предыдущий. */
    @GetMapping("/state")
    public AnalysisStateResponse state() {
        return new AnalysisStateResponse(
                analysisRunner.isRunning(),
                analysisRunner.lastRun()
                        .map(run -> new AnalysisStateResponse.LastRun(
                                run.mode(), run.startedAt(), run.finishedAt(), run.durationMs(), run.servers()))
                        .orElse(null)
        );
    }

    private LocalDate dateOrToday(LocalDate date) {
        return date == null ? LocalDate.now(clock) : date;
    }

    private AnalysisResponse toResponse(AnalysisRun run) {
        return new AnalysisResponse(
                run.mode(),
                run.startedAt(),
                run.finishedAt(),
                run.durationMs(),
                run.servers(),
                run.statuses().stream().map(apiStatusMapper::map).toList()
        );
    }
}
