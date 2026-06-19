package com.unifun.raidparser.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.unifun.raidparser.dto.ReportServerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

@Service
public class GoogleSheetsService {
    private static final Logger LOGGER = LogManager.getLogger(GoogleSheetsService.class);
    private Sheets sheetsService;

    public void upload(String spreadsheetId, String range, List<ReportServerData> reportServerDataList) throws IOException {
        if (!StringUtils.hasText(spreadsheetId) || !StringUtils.hasText(range) || reportServerDataList == null) {
            LOGGER.warn(
                    "Cannot upload data to google sheet due to incorrect spreadsheets details: Spreadsheet ID -> {}, Range -> {}, Report Data -> {}",
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

        LOGGER.info("Data uploaded to the sheet with ID {}. Data Range: {}. Result: {}", spreadsheetId, range , result);
    }

}
