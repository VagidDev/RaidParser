package com.unifun.raidparser.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.unifun.raidparser.dto.ReportServerData;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleSheetsService {
    private static final Logger LOGGER = LogManager.getLogger(GoogleSheetsService.class);

    /**
     * Провайдер, а не сам Sheets: клиент авторизуется в Google при создании,
     * поэтому создаём его только когда действительно нужен экспорт.
     */
    private final ObjectProvider<Sheets> sheetsServiceProvider;

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
        UpdateValuesResponse result = sheetsServiceProvider.getObject().spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        LOGGER.info("Data uploaded to the sheet with ID {}. Data Range: {}. Result: {}", spreadsheetId, range , result);
    }

}
