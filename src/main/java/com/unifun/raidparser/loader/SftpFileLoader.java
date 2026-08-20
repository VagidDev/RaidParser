package com.unifun.raidparser.loader;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.unifun.raidparser.config.SftpUserConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class SftpFileLoader {
    private static final Logger LOGGER = LogManager.getLogger(SftpFileLoader.class);
    private static final String PARTIAL_SUFFIX = ".part";
    private static final int CONNECT_TIMEOUT_MS = 10_000;

    private final SftpUserConfig sftpUserConfig;

    public String downloadFile(String remoteFile, String localFile) {
        String host = sftpUserConfig.getHost();
        int port = sftpUserConfig.getPort();

        Path target = Path.of(localFile);
        // Качаем в отдельный файл и переносим только по успеху: иначе оборванная
        // загрузка оставляла пустой/обрезанный отчёт, который затем бесконечно
        // отдавался из локального кэша как валидный.
        Path partial = target.resolveSibling(target.getFileName() + PARTIAL_SUFFIX);

        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = openSession(host, port);

            Channel channel = session.openChannel("sftp");
            sftpChannel = (ChannelSftp) channel;
            sftpChannel.connect(CONNECT_TIMEOUT_MS);

            if (!remoteFileExists(sftpChannel, remoteFile, host)) {
                return "";
            }

            try (InputStream inputStream = sftpChannel.get(remoteFile);
                 OutputStream outputStream = Files.newOutputStream(partial)) {
                inputStream.transferTo(outputStream);
            }

            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);

            LOGGER.info("File `{}` downloaded from sftp server `{}`! File saved to `{}`", remoteFile, host, target);
            return target.toString();
        } catch (Exception e) {
            LOGGER.error("Error while downloading file `{}` from sftp server `{}`. Error message -> {}",
                    remoteFile, host, e.getMessage(), e);
            removePartialFile(partial);
            return "";
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private Session openSession(String host, int port) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(sftpUserConfig.getLogin(), host, port);
        session.setPassword(sftpUserConfig.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(CONNECT_TIMEOUT_MS);
        return session;
    }

    private boolean remoteFileExists(ChannelSftp sftpChannel, String remoteFile, String host) {
        try {
            sftpChannel.lstat(remoteFile);
            LOGGER.info("File `{}` exists on sftp server `{}`!", remoteFile, host);
            return true;
        } catch (SftpException e) {
            LOGGER.warn("File `{}` does not exist on sftp server `{}`!", remoteFile, host);
            return false;
        }
    }

    private void removePartialFile(Path partial) {
        try {
            if (Files.deleteIfExists(partial)) {
                LOGGER.info("Removed partially downloaded file `{}`", partial);
            }
        } catch (IOException e) {
            LOGGER.error("Cannot remove partially downloaded file `{}`. Error -> {}", partial, e.getMessage(), e);
        }
    }
}
