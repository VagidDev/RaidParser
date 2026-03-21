package com.unifun.raidparser.loader;

import com.unifun.raidparser.config.SftpUserConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SftpFileLoaderTest {
    private SftpFileLoader sftpFileLoader;

    public SftpFileLoaderTest() {
        sftpFileLoader = correctConfig();
    }

    private SftpFileLoader correctConfig() {
        SftpUserConfig sftpUserConfig = new SftpUserConfig();
        sftpUserConfig.setHost("127.0.0.1");
        sftpUserConfig.setPort(2222);
        sftpUserConfig.setLogin("testuser");
        sftpUserConfig.setPassword("password");

        return new SftpFileLoader(sftpUserConfig);
    }

    private SftpFileLoader incorrectConfig() {
        SftpUserConfig sftpUserConfig = new SftpUserConfig();
        sftpUserConfig.setHost("127.0.1");
        sftpUserConfig.setPort(2222);
        sftpUserConfig.setLogin("");
        sftpUserConfig.setPassword("password");

        return new SftpFileLoader(sftpUserConfig);
    }

    @Test
    void downloadFile_SuccessfulDownloadingFile(@TempDir Path tempDir) {
        String remoteFile = "/upload/test_file_2026_03_21";
        Path localFile = tempDir.resolve("sftp_test_file");

        String result = sftpFileLoader.downloadFile(remoteFile, localFile.toFile().getAbsolutePath());

        assertEquals(localFile.toFile().getAbsolutePath(), result);
    }

    @Test
    void downloadFile_FailedDownloadingFile_NoFile(@TempDir Path tempDir) {
        String remoteFile = "/upload/file_doesnt_exists";
        Path localFile = tempDir.resolve("sftp_test_file");

        String result = sftpFileLoader.downloadFile(remoteFile, localFile.toFile().getAbsolutePath());

        assertEquals("", result);
    }

    @Test
    void downloadFile_FailedDownloadingFile_IncorrectConfig(@TempDir Path tempDir) {
        String remoteFile = "/upload/test_file_2026_03_21";
        Path localFile = tempDir.resolve("sftp_test_file");

        SftpFileLoader customSftpFileLoader = incorrectConfig();
        String result = customSftpFileLoader.downloadFile(remoteFile, localFile.toFile().getAbsolutePath());

        assertEquals("", result);
    }
}