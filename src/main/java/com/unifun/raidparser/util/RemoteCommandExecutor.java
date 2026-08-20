package com.unifun.raidparser.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.unifun.raidparser.config.SshUserConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RemoteCommandExecutor {
    private static final Logger LOGGER = LogManager.getLogger(RemoteCommandExecutor.class);
    // Регулярки для типичных интерактивных запросов
    private static final List<Pattern> INTERACTIVE_PROMPTS = List.of(
            Pattern.compile("(?i)password.*:"),
            Pattern.compile("(?i)passphrase.*:"),
            Pattern.compile("(?i)are you sure you want to continue"),
            Pattern.compile("(?i)sudo: .*password.*for.*:"),
            Pattern.compile("(?i)enter passphrase for key"),
            Pattern.compile("(?i)press any key to continue")
    );
    private static final long CLOSE_POLL_INTERVAL_MS = 50L;

    private final SshUserConfig sshUserConfig;

    private boolean checkConfig() {
        if (sshUserConfig.getLogin() == null || (sshUserConfig.getPassword() == null && sshUserConfig.getPrivateKey() == null)) {
            // Значения не логируем: пароль из конфигурации не должен попадать в логи.
            LOGGER.error("Please set up user credentials in configuration for SSH connection! " +
                            "Current configuration: `ssh.user.login` -> {}, `ssh.user.password` is set -> {}, `ssh.user.private-key` is set -> {}",
                    sshUserConfig.getLogin(),
                    sshUserConfig.getPassword() != null,
                    StringUtils.hasText(sshUserConfig.getPrivateKey())
            );
            return false;
        }
        return true;
    }

    public String execute(String host, int port, String command) {
        if (!checkConfig()) {
            return "";
        }

        if (!StringUtils.hasText(host) || port <= 0 || !StringUtils.hasText(command)) {
            LOGGER.error("Incorrect parameters for command execution! Current parameters: host -> `{}`, port -> `{}`, command -> `{}`", host, port, command);
            return "";
        }

        Session session = null;
        ChannelExec channel = null;

        try {
            session = initSession(host, port);

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            // stderr отдаём jsch в отдельный буфер: при последовательном чтении
            // stdout до конца объёмный stderr мог заполнить буфер канала
            // и заблокировать обе стороны.
            ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
            channel.setErrStream(errBuffer);

            InputStream in = channel.getInputStream();

            LOGGER.info("Connecting to the server via IP -> {} and port -> {}", host, port);
            channel.connect(sshUserConfig.getConnectTimeoutMs());

            LOGGER.info("Reading output of command: `{}`", command);
            String stdout = readStream("stdout", in);
            String stderr = prefixLines("stderr", errBuffer.toString(StandardCharsets.UTF_8));
            String output = stdout + stderr;

            awaitChannelClose(channel);
            // getExitStatus до закрытия канала всегда возвращает -1.
            LOGGER.info("Exit code for connection via IP -> {} and Port -> {} is : {}", host, port, channel.getExitStatus());

            String interactivePrompt = findInteractivePrompt(output);
            if (interactivePrompt != null) {
                LOGGER.warn("Found interactive request while executing `{}` on {}: {}", command, host, interactivePrompt);
                return "";
            }

            return output;
        } catch (Exception e) {
            LOGGER.error("Did not connect to the server via IP -> {} and Port -> {} due to: {}", host, port, e.getLocalizedMessage(), e);
            return "";
        } finally {
            if (channel != null && channel.isConnected()) channel.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    private Session initSession(String host, int port) throws JSchException {
        JSch jsch = new JSch();
        if (StringUtils.hasText(sshUserConfig.getPrivateKey())) {
            LOGGER.info("Setting private key for ssh connection to {} via port {}", host, port);
            jsch.addIdentity(sshUserConfig.getPrivateKey());
        }
        if (StringUtils.hasText(sshUserConfig.getKnownHostsFile())) {
            jsch.setKnownHosts(sshUserConfig.getKnownHostsFile());
        }

        Session session = jsch.getSession(sshUserConfig.getLogin(), host, port);
        session.setConfig("StrictHostKeyChecking", sshUserConfig.isStrictHostKeyChecking() ? "yes" : "no");

        if (sshUserConfig.getPassword() != null) {
            LOGGER.info("Setting password for ssh connection to {} via port {}", host, port);
            session.setPassword(sshUserConfig.getPassword());
        }

        // Ограничивает любое блокирующее чтение: без него зависший хост
        // навсегда занимал поток из пула проверки.
        session.setTimeout(sshUserConfig.getCommandTimeoutMs());
        session.connect(sshUserConfig.getConnectTimeoutMs());

        return session;
    }

    private String readStream(String prefix, InputStream input) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            output.append("[").append(prefix).append("] ").append(line).append("\n");
        }
        return output.toString();
    }

    private String prefixLines(String prefix, String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        StringBuilder output = new StringBuilder();
        text.lines().forEach(line -> output.append("[").append(prefix).append("] ").append(line).append("\n"));
        return output.toString();
    }

    private void awaitChannelClose(ChannelExec channel) {
        long deadline = System.currentTimeMillis() + sshUserConfig.getCommandTimeoutMs();
        while (!channel.isClosed() && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(CLOSE_POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private String findInteractivePrompt(String output) {
        for (String line : output.split("\n")) {
            for (Pattern pattern : INTERACTIVE_PROMPTS) {
                if (pattern.matcher(line).find()) {
                    return line;
                }
            }
        }
        return null;
    }
}
