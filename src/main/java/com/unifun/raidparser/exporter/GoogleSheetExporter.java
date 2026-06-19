package com.unifun.raidparser.exporter;

import com.unifun.raidparser.config.GoogleSheetExportConfig;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetExporter {
    private static final Logger LOGGER = LogManager.getLogger(GoogleSheetExporter.class);

    private final GoogleSheetsService googleSheetsService;
    private final GoogleSheetExportConfig googleSheetExportConfig;

    public <T extends Status> void export(List<ServerStatus<T>> serverStatuses, Class<T> statusClass) {
        if (serverStatuses == null || serverStatuses.isEmpty()) {
            LOGGER.warn("Got empty data for export to Google Sheets");
            return;
        }

        List<ReportServerData> reportServerDataList = serverStatuses.stream()
                .map(server -> new ReportServerData(
                                server.serverName(),
                                server.analyzeResponse().getStatus().getName(),
                                server.analyzeResponse().getErrorText()
                        )
                )
                .toList();
        String range = switch (statusClass.getSimpleName()) {
            case "DriverStatus" -> googleSheetExportConfig.getDiskRange();
            case "PowerSupplyStatus" -> googleSheetExportConfig.getPsuRange();
            case "BatteryStatus" -> googleSheetExportConfig.getBatteryRange();
            default -> throw new IllegalArgumentException("Unknown status type: " + statusClass);
        };

        try {
            googleSheetsService.upload(googleSheetExportConfig.getSpreadsheetId(), range, reportServerDataList);
        } catch (Exception e) {
            LOGGER.error("Error, while trying export data to google-sheet. Trying to remove old token, to fix error. Error message -> {}", e.getMessage(), e);
        }
    }

}
