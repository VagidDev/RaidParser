package com.unifun.raidparser.exporter;

import com.unifun.raidparser.config.GoogleSheetExportConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ReportServerData;
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

    public void export(List<ReportServerData> reportServerData, HealthType healthType) {
        if (reportServerData == null || healthType == null || reportServerData.isEmpty()) {
            LOGGER.warn("Got empty data for export to Google Sheets. Report Data -> {} Health Type -> {}", reportServerData, healthType);
            return;
        }

        String range = switch (healthType) {
            case DRIVE_HEALTH -> googleSheetExportConfig.getDiskRange();
            case PSU_HEALTH -> googleSheetExportConfig.getPsuRange();
            case BATTERY_HEALTH -> googleSheetExportConfig.getBatteryRange();
            default -> throw new IllegalArgumentException("Unknown status type: " + healthType);
        };

        try {
            googleSheetsService.upload(googleSheetExportConfig.getSpreadsheetId(), range, reportServerData);
        } catch (Exception e) {
            LOGGER.error("Error, while trying upload data to google-sheet. Error message -> {}", e.getMessage(), e);
        }
    }

}
