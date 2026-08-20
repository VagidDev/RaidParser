package com.unifun.raidparser.mapper;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.dto.ServerStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ExportDataMapper {
    private static final Logger LOGGER = LogManager.getLogger(ExportDataMapper.class);
    private static final String NO_DATA_STATUS = "NO DATA";

    public List<ReportServerData> map(List<ServerStatus> serverStatuses, HealthType type) {
        if (serverStatuses == null) {
            LOGGER.warn("Got null instead of server statuses for health type {}", type);
            return List.of();
        }

        return serverStatuses.stream()
                .filter(Objects::nonNull)
                .map(serverStatus -> toReportServerData(serverStatus, type))
                .toList();
    }

    /**
     * Статус по типу может отсутствовать: например, для хоста в servers-to-check
     * настроены команды не на все компоненты. Такой сервер должен попасть в отчёт
     * с явной отметкой, а не ронять экспорт по NPE.
     */
    private ReportServerData toReportServerData(ServerStatus serverStatus, HealthType type) {
        AnalyzeResponse<? extends Status> response = serverStatus.healthStatusMap().get(type);

        if (response == null || response.getStatus() == null) {
            LOGGER.warn("Server `{}` has no {} status, exporting it as `{}`", serverStatus.serverName(), type, NO_DATA_STATUS);
            return new ReportServerData(serverStatus.serverName(), NO_DATA_STATUS, "");
        }

        String errorText = response.getErrorText() == null ? "" : response.getErrorText();
        return new ReportServerData(serverStatus.serverName(), response.getStatus().getName(), errorText);
    }
}
