package com.unifun.raidparser.service;

import com.unifun.raidparser.config.HostExecutorConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerTask;
import com.unifun.raidparser.util.RemoteCommandExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
    private final HostExecutorConfig hostExecutorConfig;

    private ExecutorService sshExecutor;

    @PostConstruct
    public void initialize() {
        this.sshExecutor = Executors.newFixedThreadPool(
                hostExecutorConfig.getThreads()
        );
    }

    public List<ServerData> execute(List<ServerTask> serverTasks) {
        try  {
            List<Future<ServerData>> serverDataFutureList = new ArrayList<>();

            for (ServerTask serverTask : serverTasks) {
                Future<ServerData> serverDataFuture = sshExecutor.submit(() -> executeTask(serverTask));
                serverDataFutureList.add(serverDataFuture);
            }

            List<ServerData> serverDataList = new ArrayList<>();
            for (Future<ServerData> future : serverDataFutureList) {
                try {
                    ServerData serverData = future.get();
                    serverDataList.add(serverData);
                } catch (InterruptedException e) {
                    LOGGER.warn("Try to interrupt task in thread {}; Exception -> {}", Thread.currentThread().getName(), e.getLocalizedMessage(), e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    LOGGER.error("Fatal error while executing task in thread {}; Error -> {}", Thread.currentThread().getName(), e.getLocalizedMessage(), e);
                }
            }

            return serverDataList;
        } catch (Exception e) {
            LOGGER.error("Some error while trying to execute tasks, error message -> {}", e.getLocalizedMessage(), e);
            return List.of();
        }
    }

    private ServerData executeTask(ServerTask serverTask) {
        Map<HealthType, String> healthOutput = new HashMap<>();

        for (Map.Entry<HealthType, String> entry : serverTask.healthCommand().entrySet()) {
            LOGGER.debug("Executing for host -> `{}` with ip -> `{}` command -> {} for health type -> {}",
                    entry.getValue(), entry.getKey(), serverTask.ip(), serverTask.port());
            String output = remoteCommandExecutor.execute(serverTask.ip(), serverTask.port(), entry.getValue());
            LOGGER.debug("Got output `{}` for command -> {}", entry.getValue(), output);
            healthOutput.put(entry.getKey(), output);
        }

        ServerData data = new ServerData(serverTask.hostname(), healthOutput);
        LOGGER.debug("Creating Server Data -> {}", data);
        return data;
    }

    @PreDestroy
    public void destroy() {
        try  {
            if (sshExecutor != null && !sshExecutor.isShutdown()) {
                LOGGER.info("Shutting down HostExecutorService thread pool...");
                sshExecutor.shutdown();
            }
        } catch (Exception e) {
            LOGGER.error("Cannot shutdown thread pool, error message -> {}", e.getLocalizedMessage(), e);
        }
    }
}
