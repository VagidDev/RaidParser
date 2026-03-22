package com.unifun.raidparser.service;

import com.unifun.raidparser.config.LocalFileRuleConfig;
import com.unifun.raidparser.config.RemoteFileRuleConfig;
import com.unifun.raidparser.config.SftpUserConfig;
import com.unifun.raidparser.loader.SftpFileLoader;
import com.unifun.raidparser.parser.DateParser;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class SftpFileServiceTest {
    private final LocalDate testDate = LocalDate.parse("2026-03-21", DateTimeFormatter.ISO_DATE);
    private SftpFileService sftpFileService;
    @TempDir
    Path testDir;

    private SftpFileLoader correctSftpConfig() {
        SftpUserConfig sftpUserConfig = new SftpUserConfig();
        sftpUserConfig.setHost("127.0.0.1");
        sftpUserConfig.setPort(2222);
        sftpUserConfig.setLogin("testuser");
        sftpUserConfig.setPassword("password");

        return new SftpFileLoader(sftpUserConfig);
    }

    private SftpFileService initConfiguration() {
        RemoteFileRuleConfig remoteFileRuleConfig = new RemoteFileRuleConfig();
        remoteFileRuleConfig.setDirectory("/upload/");
        remoteFileRuleConfig.setMask("test_file_{date}");
        remoteFileRuleConfig.setDateFormat("yyyy_MM_dd");

        LocalFileRuleConfig localFileRuleConfig = new LocalFileRuleConfig();
        localFileRuleConfig.setDirectory(testDir.toString());
        localFileRuleConfig.setMask("local_test_file_{date}");
        localFileRuleConfig.setDateFormat("yyyy_MM_dd");

        SftpFileLoader sftpFileLoader = correctSftpConfig();

        DateParser dateParser = new DateParser(null);

        return new SftpFileService(
                remoteFileRuleConfig,
                localFileRuleConfig,
                sftpFileLoader,
                dateParser
        );
    }

    @BeforeEach
    public void init() {
        sftpFileService = initConfiguration();
    }

    @Test
    void getFileForDate_SuccessfulDownloadingFile() {
        Path testLocalFile = testDir.resolve("local_test_file_".concat(testDate.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))));
        String savedFile = sftpFileService.getFileForDate(testDate);
        assertTrue(Files.exists(testLocalFile));
    }

    @Test
    @SneakyThrows
    void getFileForDate_SuccessfulDownloadingFileWithCreatingDirectory() {
        Path testLocalFile = testDir.resolve("local_test_file_".concat(testDate.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))));
        Files.deleteIfExists(testDir);
        String savedFile = sftpFileService.getFileForDate(testDate);
        assertTrue(Files.exists(testLocalFile));
    }

    @Test
    @SneakyThrows
    void getFileForDate_SuccessfulGettingFileFromLocalDir() {
        Path testLocalFile = testDir.resolve("local_test_file_".concat(testDate.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))));
        Files.createFile(testLocalFile);
        String savedFile = sftpFileService.getFileForDate(testDate);
        assertEquals(testLocalFile.toString(), savedFile);
    }

    @Test
    void getFileForDate_FailedDownloading_FileDoesNotExistsOnSftp() {
        //Path testLocalFile = testDir.resolve("local_test_file_".concat(doesNotExistingDate.format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))));
        LocalDate doesNotExistingDate = LocalDate.parse("2026-03-22", DateTimeFormatter.ISO_DATE);
        String savedFile = sftpFileService.getFileForDate(doesNotExistingDate);
        assertEquals("", savedFile);
    }

}