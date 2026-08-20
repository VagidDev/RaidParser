package com.unifun.raidparser.exporter;

import com.unifun.raidparser.config.GoogleSheetExportConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.service.GoogleSheetsService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetExporter {
    private static final Logger LOGGER = LogManager.getLogger(GoogleSheetExporter.class);

    private final GoogleSheetsService googleSheetsService;
    private final GoogleSheetExportConfig googleSheetExportConfig;

    /**
     * @return true, если данные действительно ушли в таблицу. Раньше метод был void,
     *         и вызывающий код сообщал об успехе даже когда выгрузка не состоялась.
     */
    public boolean export(List<ReportServerData> reportServerData, HealthType healthType) {
        if (reportServerData == null || healthType == null || reportServerData.isEmpty()) {
            LOGGER.warn("Got empty data for export to Google Sheets. Report Data -> {} Health Type -> {}", reportServerData, healthType);
            return false;
        }

        String range = switch (healthType) {
            case DRIVE_HEALTH -> googleSheetExportConfig.getDiskRange();
            case PSU_HEALTH -> googleSheetExportConfig.getPsuRange();
            case BATTERY_HEALTH -> googleSheetExportConfig.getBatteryRange();
            default -> throw new IllegalArgumentException("Unknown status type: " + healthType);
        };

        if (!StringUtils.hasText(googleSheetExportConfig.getSpreadsheetId()) || !StringUtils.hasText(range)) {
            LOGGER.error("Google Sheets export is not configured: `sheets.export.spreadsheet-id` -> `{}`, range for {} -> `{}`",
                    googleSheetExportConfig.getSpreadsheetId(), healthType, range);
            return false;
        }

        try {
            googleSheetsService.upload(googleSheetExportConfig.getSpreadsheetId(), range, reportServerData);
            return true;
        } catch (Exception e) {
            LOGGER.error("Error, while trying upload data to google-sheet. Error message -> {}", e.getMessage(), e);
            return false;
        }
    }

}
