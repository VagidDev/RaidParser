package com.unifun.raidparser.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.unifun.raidparser.config.GoogleSheetAuthorizationConfig;
import com.unifun.raidparser.config.GoogleSheetExportConfig;
import com.unifun.raidparser.dto.ReportServerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Service
public class GoogleSheetsService {
    private static final Logger LOGGER = LogManager.getLogger(GoogleSheetsService.class);

    private GoogleSheetAuthorizationConfig googleSheetAuthorizationConfig;
    private GoogleSheetExportConfig googleSheetExportConfig;
    private Sheets sheetsService;

    public void export(ServerDataType dataType, List<ReportServerData> reportServerData) {
        try {
            switch (dataType) {
                case HARD_DRIVE_DATA -> exportToSheet(
                        googleSheetExportConfig.getSpreadsheetId(),
                        googleSheetExportConfig.getDiskRange(),
                        reportServerData
                );
                case RAID_BATTERY_DATA -> exportToSheet(
                        googleSheetExportConfig.getSpreadsheetId(),
                        googleSheetExportConfig.getBatteryRange(),
                        reportServerData
                );
                case POWER_SUPPLY_UNITS_DATA -> exportToSheet(
                        googleSheetExportConfig.getSpreadsheetId(),
                        googleSheetExportConfig.getPsuRange(),
                        reportServerData
                );
            }
        } catch (Exception e) {
            //TODO: analyze error message when stored credentials (tokens) are expired
            LOGGER.error("Error, while trying export data to google-sheet. Trying to remove old token, to fix error. Error message -> {}", e.getMessage(), e);
            removeOldCredentials(googleSheetAuthorizationConfig.getSavingTokensDir());
            /*
             GoogleSheetsExporter.removeOldCredentials();
             System.err.println("Old credentials were deleted, try again please");
            */
        }
    }

    private void exportToSheet(String spreadsheetId, String range, List<ReportServerData> reportServerDataList) throws IOException {
        if (!StringUtils.hasText(spreadsheetId) || !StringUtils.hasText(range) || reportServerDataList == null) {
            LOGGER.warn(
                    "Cannot export data to google sheet due to incorrect spreadsheets details: Spreadsheet ID -> {}, Range -> {}, Report Data -> {}",
                    spreadsheetId, range, reportServerDataList
            );
            return;
        }

        List<List<Object>> values = new ArrayList<>();
        for (ReportServerData data : reportServerDataList) {
            List<Object> row = new ArrayList<>(List.of(
                    data.serverName(),
                    data.healthStatus().trim(),
                    data.errorText().trim()
            ));
            values.add(row);
        }

        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        LOGGER.info("Data exported to the sheet with ID {}. Data Range: {}. Result: {}", spreadsheetId, range , result);
    }

    //TODO: Analyze error that trows export method
    public void removeOldCredentials(String credentialPath) {
        Path removingPath = Path.of(credentialPath);
        try (Stream<Path> storedCredentials = Files.walk(removingPath)) {

            List<Path> paths = storedCredentials
                    .filter(path -> path.compareTo(removingPath) != 0)
                    .toList();

            for (Path pathToDelete : paths) {
                boolean isDeleted = Files.deleteIfExists(pathToDelete);
                if (isDeleted) {
                    LOGGER.info("Credentials were deleted: {} ", pathToDelete.getFileName());
                } else {
                    LOGGER.error("Cannot delete credential: {}", pathToDelete.getFileName());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Input/Output exception while deleting old credential! Path to credentials -> {}, StackTrace -> {}", removingPath, e.getMessage(), e);
        }
    }

}
