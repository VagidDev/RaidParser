package com.unifun.raidparser.service;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerTask;
import com.unifun.raidparser.util.RemoteCommandExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class HostExecutorService {
    private static final Logger LOGGER = LogManager.getLogger(HostExecutorService.class);

    private final RemoteCommandExecutor remoteCommandExecutor;

    public List<ServerData> execute(List<ServerTask> serverTasks) {
        int maxParallelConnections = 8;
        try (ThreadPoolExecutor sshExecutor = new ThreadPoolExecutor(
                maxParallelConnections,
                maxParallelConnections,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        )) {
            List<Future<ServerData>> serverDataFutureList = new ArrayList<>();

            for (ServerTask serverTask : serverTasks) {
                Future<ServerData> serverDataFuture = sshExecutor.submit(() -> executeTask(serverTask));
                serverDataFutureList.add(serverDataFuture);
            }

            return List.of();
        } catch (Exception e) {
            LOGGER.error("Some error while trying to execute tasks, error message -> {}", e.getLocalizedMessage(), e);
            return List.of();
        }
    }

    private ServerData executeTask(ServerTask serverTask) {
        Map<HealthType, String> healthOutput = new HashMap<>();

        for (Map.Entry<HealthType, String> entry : serverTask.healthCommand().entrySet()) {
            String output = remoteCommandExecutor.execute(serverTask.ip(), serverTask.port(), entry.getValue());
            healthOutput.put(entry.getKey(), output);
        }

        return new ServerData(
                serverTask.hostname(), healthOutput
        );
    }
}
