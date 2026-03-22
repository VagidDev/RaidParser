package com.unifun.raidparser.service;

import com.unifun.raidparser.config.LocalFileRuleConfig;
import com.unifun.raidparser.config.RemoteFileRuleConfig;
import com.unifun.raidparser.loader.SftpFileLoader;
import com.unifun.raidparser.parser.DateParser;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SftpFileService {
    private static final Logger LOGGER = LogManager.getLogger(SftpFileService.class);

    private final RemoteFileRuleConfig remoteFileRuleConfig;
    private final LocalFileRuleConfig localFileRuleConfig;
    private final SftpFileLoader sftpFileLoader;
    private final DateParser dateParser;

    public Path getFileForDate(LocalDate date) {
        Path localFile = buildLocalFile(date);
        LOGGER.debug("Got local file path -> {}", localFile);
        if (localFile == null) {
            LOGGER.error("Cannot get local file for saving/reading from sftp");
            return null;
        }

        if (Files.exists(localFile)) {
            LOGGER.info("Getting file from local saving dir. Returning file {}", localFile);
            return localFile;
        }

        return getFileFromSftp(date, localFile);
    }

    @Nullable
    private Path buildLocalFile(LocalDate date) {
        Path savingDir = Path.of(localFileRuleConfig.getDirectory());
        if (!ensureDir(savingDir)) {
            LOGGER.error("Cannot ensure directory `{}` to save files", savingDir);
            return null;
        }

        String localFileName = localFileRuleConfig
                .getMask()
                .replace("{date}", dateParser
                        .parseToString(date, localFileRuleConfig.getDateFormat())
                );

        return savingDir.resolve(localFileName);
    }

    private boolean ensureDir(Path dir) {
        if (Files.exists(dir)) {
            LOGGER.info("Directory {} exists", dir);
            return true;
        }

        try {
            LOGGER.warn("Directory {} does not exists! Creating directory...", dir);
            Files.createDirectories(dir);
            return true;
        } catch (IOException e) {
            LOGGER.error("Cannot create directory {} due to {}", dir, e.getLocalizedMessage(), e);
            return false;
        }
    }

    private Path getFileFromSftp(LocalDate date, Path savingPath) {
        Path remoteFilePath = buildRemoteFile(date);
        LOGGER.info("Getting file from sftp `{}` and saving to local file `{}`", remoteFilePath, savingPath);
        String downloadedFilePath = sftpFileLoader.downloadFile(remoteFilePath.toString(), savingPath.toString());
        if (downloadedFilePath.isBlank()) {
            LOGGER.warn("Cannot download file `{}` from sftp", remoteFilePath);
            return null;
        }
        return Path.of(downloadedFilePath);
    }

    private Path buildRemoteFile(LocalDate date) {
        String remoteFileName = remoteFileRuleConfig
                .getMask()
                .replace("{date}", dateParser
                        .parseToString(date, remoteFileRuleConfig.getDateFormat())
                );
        return Paths.get(remoteFileRuleConfig.getDirectory(), remoteFileName);
    }
}
