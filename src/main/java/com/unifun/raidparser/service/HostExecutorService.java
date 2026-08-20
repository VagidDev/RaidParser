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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class HostExecutorService {
    private static final Logger LOGGER = LogManager.getLogger(HostExecutorService.class);
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30L;

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
                    // С таймаутом: один зависший хост не должен останавливать всю проверку.
                    serverDataList.add(future.get(hostExecutorConfig.getTaskTimeoutSeconds(), TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                    LOGGER.warn("Interrupted while waiting for host task; Exception -> {}", e.getLocalizedMessage(), e);
                    future.cancel(true);
                    Thread.currentThread().interrupt();
                    break;
                } catch (TimeoutException e) {
                    LOGGER.error("Host task did not finish in {} seconds, cancelling it", hostExecutorConfig.getTaskTimeoutSeconds());
                    future.cancel(true);
                } catch (ExecutionException e) {
                    LOGGER.error("Fatal error while executing host task; Error -> {}", e.getLocalizedMessage(), e);
                }
            }

            return serverDataList;
        } catch (Exception e) {
            LOGGER.error("Some error while trying to execute tasks, error message -> {}", e.getLocalizedMessage(), e);
            return List.of();
        }
    }

    private ServerData executeTask(ServerTask serverTask) {
        Map<HealthType, String> healthOutput = new EnumMap<>(HealthType.class);

        for (Map.Entry<HealthType, String> entry : serverTask.healthCommand().entrySet()) {
            HealthType healthType = entry.getKey();
            String command = entry.getValue();

            LOGGER.debug("Executing for host -> `{}` with ip -> `{}` and port -> `{}` command -> `{}` for health type -> {}",
                    serverTask.hostname(), serverTask.ip(), serverTask.port(), command, healthType);
            String output = remoteCommandExecutor.execute(serverTask.ip(), serverTask.port(), command);
            LOGGER.debug("Got output `{}` for command -> `{}`", output, command);

            healthOutput.put(healthType, output);
        }

        ServerData data = new ServerData(serverTask.hostname(), healthOutput);
        LOGGER.debug("Creating Server Data -> {}", data);
        return data;
    }

    @PreDestroy
    public void destroy() {
        if (sshExecutor == null || sshExecutor.isShutdown()) {
            return;
        }
        try  {
            LOGGER.info("Shutting down HostExecutorService thread pool...");
            sshExecutor.shutdown();
            if (!sshExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                LOGGER.warn("Thread pool did not terminate in {} seconds, forcing shutdown", SHUTDOWN_TIMEOUT_SECONDS);
                sshExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            sshExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("Cannot shutdown thread pool, error message -> {}", e.getLocalizedMessage(), e);
        }
    }
}
