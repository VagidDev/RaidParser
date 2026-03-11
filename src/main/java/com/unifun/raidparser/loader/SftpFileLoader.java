package com.unifun.raidparser.loader;

import com.jcraft.jsch.*;
import com.unifun.raidparser.config.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SftpFileLoader {
    private static final String DEFAULT_SAVING_DIR = "./raid_reports/";
    private static final String DEFAULT_REMOTE_FILE_TEMPLATE = "/servers_raid_status_";

    private static final Logger LOGGER = LogManager.getLogger(SftpFileLoader.class);

    private static String getFileOnLocalServer(String date) {
        String filePath = "";

        //checking if date format is correct
        if (date.matches("^\\d{4}_(0[1-9]|1[0-2])_(0[1-9]|[12][0-9]|3[01])$")) {
            if (AppConfig.get("dir.raid-reports").isBlank()) {
                filePath = SftpFileLoader.DEFAULT_SAVING_DIR + "servers_raid_status_" + date;
                LOGGER.info("Getting default saving directory: {} ", filePath);
                return filePath;
            }
            filePath = AppConfig.get("dir.raid-reports") + "servers_raid_status_" + date;
            LOGGER.info("Getting saving directory from config: {} ", filePath);
            return filePath;
        } else {
            LOGGER.warn("Entered invalid date, that does not matches pattern for yyyy_MM_dd. Entered date: {} ", date);
            return "";
        }
    }

    public static String getFileForDate(String date) {
        String path = getFileOnLocalServer(date);
        if (path.isBlank() || !Files.isRegularFile(Path.of(path))) {
            LOGGER.info("File do not exists on local server and will be downloaded from SFTP");
            path = downloadFileFromRemoteServer(date);
        }

        return path;
    }

    private static String downloadFileFromRemoteServer(String date) {
        String host = AppConfig.get("sftp.host"); // Адрес удаленного сервера
        String username = AppConfig.get("sftp.user");       // Логин
        String password = AppConfig.get("sftp.password");       // Пароль
        String remoteFile = (AppConfig.get("sftp.remote-file-template").isBlank()
                ? DEFAULT_REMOTE_FILE_TEMPLATE : AppConfig.get("sftp.remote-file-template"))
            + date; // Путь к файлу на удаленном сервере
        String localDir = AppConfig.get("sftp.local-path-raid-status").isBlank() ? DEFAULT_SAVING_DIR : AppConfig.get("sftp.local-path-raid-status"); // Локальный путь для сохранения файла
        String localFile = localDir + "servers_raid_status_" + date;

        if (host.isEmpty() || username.isEmpty() || password.isEmpty()) {
            LOGGER.error("Please set up sftp host and user credentials in configuration!");
            return "";
        }

        Path localPath = Path.of(localDir);
        if (Files.notExists(localPath)) {
            LOGGER.warn("Directory `{}` for saving reports from sftp does not exists.", localPath);
            try {
                Files.createDirectory(localPath);
                LOGGER.info("Directory `{}` for saving reports from sftp is created", localPath);
            } catch (IOException e) {
                LOGGER.error("Unexpected error while creating local directory `{}` for saving reports from sftp server. Error -> {}",
                        localPath, e.getMessage(), e);
            }
        }

        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp sftpChannel = null;

        try {
            // Создаем SSH сессию
            session = jsch.getSession(username, host, 22);
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
}
