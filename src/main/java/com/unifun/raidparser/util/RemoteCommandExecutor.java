package com.unifun.raidparser.util;

import com.jcraft.jsch.*;
import com.unifun.raidparser.config.SshUserConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
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

    private final SshUserConfig sshUserConfig;

    private boolean checkConfig() {
        if (sshUserConfig.getLogin() == null || (sshUserConfig.getPassword() == null && sshUserConfig.getPrivateKey() == null)) {
            LOGGER.error("Please set up user credentials in configuration for SSH connection! " +
                    "Current configuration: `ssh.user.login` -> {}, `ssh.user.password` -> {}, ssh.user.private-key -> {}",
                    sshUserConfig.getLogin(),
                    sshUserConfig.getPassword(),
                    sshUserConfig.getPrivateKey()
            );
            return false;
        }
        return true;
    }

    public String execute(String host, int port, String command) {
        if (!checkConfig()) {
            return "";
        }

        if (host.isEmpty() || port <= 0 || command.isEmpty()) {
            LOGGER.error("Incorrect parameters for command execution! Current parameters: host -> `{}`, port -> `{}`, command -> `{}`", host, port, command);
            return "";
        }

        Session session = null;
        ChannelExec channel = null;
        StringBuilder output = new StringBuilder();

        try {
            session = initSession(host, port);

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            InputStream in = channel.getInputStream();
            InputStream err = channel.getErrStream();

            LOGGER.info("Connecting to the server via IP -> {} and port -> {}", host, port);
            channel.connect();

            LOGGER.info("Reading output of command: `{}`", command);
            // проверяем stdout и stderr
            output.append(readStream("stdout", in, channel, session));
            output.append(readStream("stderr", err, channel, session));

            int exitCode = channel.getExitStatus();
            LOGGER.info("Exit code for connection via IP -> {} and Port -> {} is : {}", host, port, exitCode);
            return output.toString();
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

        Session session = jsch.getSession(sshUserConfig.getLogin(), host, port);
        session.setConfig("StrictHostKeyChecking", "no");

        if (sshUserConfig.getPassword() != null) {
            LOGGER.info("Setting password for ssh connection to {} via port {}", host, port);
            session.setPassword(sshUserConfig.getPassword());
        }

        session.connect(10000);

        return session;
    }

    private String readStream(String prefix, InputStream input,
                                   ChannelExec channel,
                                   Session session) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = br.readLine()) != null) {
            // проверка на интерактивный запрос
            if (isInteractivePrompt(line)) {
                LOGGER.warn("Found interactive request: {}", line);
                // разрываем соединение
                if (channel != null && channel.isConnected()) channel.disconnect();
                if (session != null && session.isConnected()) session.disconnect();
                throw new RuntimeException("Stopped due to interactive query: " + line);
            }
            output.append("[").append(prefix).append("] ").append(line).append("\n");
        }
        return output.toString();
    }

    private boolean isInteractivePrompt(String line) {
        for (Pattern p : INTERACTIVE_PROMPTS) {
            if (p.matcher(line).find()) return true;
        }
        return false;
    }
}
