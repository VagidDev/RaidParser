package com.unifun.raidparser.service;

import com.unifun.raidparser.dto.ServerStatus;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnalysisRunnerTest {

    private final RaidParserService raidParserService = mock(RaidParserService.class);
    private final SftpFileService sftpFileService = mock(SftpFileService.class);
    private final AnalysisRunner analysisRunner = new AnalysisRunner(raidParserService, sftpFileService, Clock.systemUTC());

    private ServerStatus status(String name) {
        return new ServerStatus(name, new ConcurrentHashMap<>());
    }

    @Test
    void runHostAnalysis_returnsStatusesAndTiming() {
        when(raidParserService.analyzeStatusFromHosts()).thenReturn(List.of(status("host-01"), status("host-02")));

        AnalysisRun run = analysisRunner.runHostAnalysis();

        assertEquals(AnalysisRunner.MODE_HOSTS, run.mode());
        assertEquals(2, run.servers());
        assertTrue(run.durationMs() >= 0);
        assertEquals(run, analysisRunner.lastRun().orElseThrow());
    }

    @Test
    void runReportAnalysis_reportMissing_throwsReportNotFound() {
        when(sftpFileService.getFileForDate(any())).thenReturn(null);

        assertThrows(ReportNotFoundException.class, () -> analysisRunner.runReportAnalysis(LocalDate.of(2026, 8, 20)));
        verify(raidParserService, never()).analyzeStatusFromReportFile(any());
    }

    @Test
    void runFullAnalysis_combinesReportAndHostStatuses() {
        when(sftpFileService.getFileForDate(any())).thenReturn(Path.of("report"));
        when(raidParserService.analyzeStatusFromReportFile(any())).thenReturn(List.of(status("host-01")));
        when(raidParserService.analyzeStatusFromHosts()).thenReturn(List.of(status("host-02")));

        AnalysisRun run = analysisRunner.runFullAnalysis(LocalDate.of(2026, 8, 20));

        assertEquals(AnalysisRunner.MODE_FULL, run.mode());
        assertEquals(2, run.servers());
    }

    @Test
    void secondConcurrentRun_isRejected() throws Exception {
        CountDownLatch analysisStarted = new CountDownLatch(1);
        CountDownLatch releaseAnalysis = new CountDownLatch(1);

        when(raidParserService.analyzeStatusFromHosts()).thenAnswer(invocation -> {
            analysisStarted.countDown();
            assertTrue(releaseAnalysis.await(5, TimeUnit.SECONDS));
            return List.of(status("host-01"));
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            executor.submit(analysisRunner::runHostAnalysis);
            assertTrue(analysisStarted.await(5, TimeUnit.SECONDS));

            assertTrue(analysisRunner.isRunning());
            assertThrows(AnalysisAlreadyRunningException.class, analysisRunner::runHostAnalysis);
        } finally {
            releaseAnalysis.countDown();
            executor.shutdown();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }

        assertFalse(analysisRunner.isRunning());
    }
}
