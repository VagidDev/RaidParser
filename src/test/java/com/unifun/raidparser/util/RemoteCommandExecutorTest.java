package com.unifun.raidparser.util;

import com.unifun.raidparser.config.SshUserConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RemoteCommandExecutorTest {
    private final static String HOST = "127.0.0.1";
    private final static int PORT = 2223;
    private final static String LOGIN = "testuser";
    private final static String PASSWORD = "password";

    private RemoteCommandExecutor remoteCommandExecutor;

    private void init() {
        SshUserConfig sshUserConfig = new SshUserConfig();
        sshUserConfig.setLogin(LOGIN);
        sshUserConfig.setPassword(PASSWORD);
        remoteCommandExecutor = new RemoteCommandExecutor(sshUserConfig);
    }

    public RemoteCommandExecutorTest() {
        init();
    }

    @Test
    void execute_SuccessfulExecutedCommand() {
        String command = "echo \"Hello World!\"";
        String output = remoteCommandExecutor.execute(HOST, PORT, command);
        System.out.println(output);
        assertTrue(output.contains("Hello World!"));
    }

    @Test
    void execute_FailedExecutedCommand_IncorrectHost() {
        String command = "echo \"Hello World!\"";
        String output = remoteCommandExecutor.execute("18.10.10", PORT, command);
        assertEquals("", output);
    }

    @Test
    void execute_FailedExecutedCommand_EmptySshUser() {
        String command = "echo \"Hello World!\"";
        RemoteCommandExecutor customRemoteCommandExecutor = new RemoteCommandExecutor(new SshUserConfig());
        String output = customRemoteCommandExecutor.execute(HOST, PORT, command);
        assertEquals("", output);
    }

    @Test
    void execute_FailedExecutedCommand_IncorrectCredentials() {
        String command = "echo \"Hello World!\"";
        SshUserConfig customSshUser = new SshUserConfig();
        customSshUser.setLogin("custom");
        customSshUser.setPassword("custom");
        RemoteCommandExecutor customRemoteCommandExecutor = new RemoteCommandExecutor(customSshUser);
        String output = customRemoteCommandExecutor.execute(HOST, PORT, command);
        assertEquals("", output);
    }
}