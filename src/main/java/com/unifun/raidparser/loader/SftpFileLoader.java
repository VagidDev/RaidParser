package com.unifun.raidparser.loader;

import com.jcraft.jsch.*;
import com.unifun.raidparser.config.AppConfig;
import com.unifun.raidparser.config.SftpUserConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class SftpFileLoader {
    private static final Logger LOGGER = LogManager.getLogger(SftpFileLoader.class);

    private final SftpUserConfig sftpUserConfig;

    public String downloadFile(String remoteFile, String localFile) {
        String host = sftpUserConfig.getHost();             // Адрес удаленного сервера
        int port = sftpUserConfig.getPort();                // Port
        String username = sftpUserConfig.getLogin();        // Логин
        String password = sftpUserConfig.getPassword();     // Пароль

        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;

        try {
            // Создаем SSH сессию
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            // Отключаем проверку хоста (если необходимо)
            session.setConfig("StrictHostKeyChecking", "no");

            // Устанавливаем соединение
            session.connect();

            // Открываем SFTP канал
            channel = session.openChannel("sftp");
            sftpChannel = (ChannelSftp) channel;
            sftpChannel.connect();

            try {
                sftpChannel.lstat(remoteFile);  // Пытаемся получить информацию о файле
                LOGGER.info("File `{}` exists on sftp server `{}`!", remoteFile, host);
            } catch (SftpException e) {
                LOGGER.warn("File `{}` does not exists on sftp server `{}`!", remoteFile, host);
                return "";
            }

            // Скачиваем файл
            InputStream inputStream = sftpChannel.get(remoteFile);
            FileOutputStream fileOutputStream = new FileOutputStream(localFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            // Закрываем потоки и каналы
            fileOutputStream.close();
            inputStream.close();
            sftpChannel.exit();
            session.disconnect();

            LOGGER.info("File `{}` downloaded from sftp server `{}`! File saved to `{}`", remoteFile, host, localFile);
            return localFile;

        } catch (Exception e) {
            LOGGER.error("Error while downloading file `{}` form sftp server `{}`. Error message -> {}",
                    remoteFile, host, e.getMessage(), e);
            return "";
        }
    }

//    String remoteFile = (AppConfig.get("sftp.remote-file-template").isBlank()
//            ? DEFAULT_REMOTE_FILE_TEMPLATE : AppConfig.get("sftp.remote-file-template"))
//            + date; // Путь к файлу на удаленном сервере
//    String localDir = AppConfig.get("sftp.local-path-raid-status").isBlank() ? DEFAULT_SAVING_DIR : AppConfig.get("sftp.local-path-raid-status"); // Локальный путь для сохранения файла
//    String localFile = localDir + "servers_raid_status_" + date;
//
//        if (host.isEmpty() || username.isEmpty() || password.isEmpty()) {
//        LOGGER.error("Please set up sftp host and user credentials in configuration!");
//        return "";
//    }
//
//    Path localPath = Path.of(localDir);
//        if (Files.notExists(localPath)) {
//        LOGGER.warn("Directory `{}` for saving reports from sftp does not exists.", localPath);
//        try {
//            Files.createDirectory(localPath);
//            LOGGER.info("Directory `{}` for saving reports from sftp is created", localPath);
//        } catch (IOException e) {
//            LOGGER.error("Unexpected error while creating local directory `{}` for saving reports from sftp server. Error -> {}",
//                    localPath, e.getMessage(), e);
//        }
//    }
}
