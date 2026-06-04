package com.unifun.raidparser.sheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.service.RaidParserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.unifun.raidparser.config.GoogleSheetExporterConfig;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Stream;

@Service
public class GoogleSheetsExporter {
    private static final Logger LOGGER = LogManager.getLogger(GoogleSheetsExporter.class);
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private GoogleSheetExporterConfig googleSheetsExporterConfig;
    private RaidParserService raidParserService;
    private Sheets sheetsService;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.

    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
     */

    private void initialize() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.sheetsService =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {

        // Load client secrets.
        InputStream in = Files.newInputStream(Path.of(googleSheetsExporterConfig.getUserCredentialsJson()), StandardOpenOption.READ);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Define required scopes for Sheets and Drive
        List<String> SCOPES = List.of(
                "https://www.googleapis.com/auth/spreadsheets"  // Scope for Sheets
//                "https://www.googleapis.com/auth/drive"         // Optional: Scope for Drive
        );
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(googleSheetsExporterConfig.getSavingTokenPath())))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public void export(String path) {
        try {
            exportToSheet(path);
        } catch (Exception e) {
            //TODO: analyze error message when stored credentials (tokens) are expired
            LOGGER.error("Error, while trying export data to google-sheet. Error message -> {}", e.getMessage(), e);
            /*
             GoogleSheetsExporter.removeOldCredentials();
             System.err.println("Old credentials were deleted, try again please");
            */
        }
    }

    /**
     * Prints the names and majors of students in a sample spreadsheet:
     * <a href="https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit">...</a>
     */
    private void exportToSheet(String path) throws Exception {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
    }


    private void writeToSheet(Sheets service, String spreadsheetId, String range, List<ReportServerData> reportServerDataList) throws IOException {
        if (range.isEmpty()) {
            LOGGER.warn("Range is empty");
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
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        LOGGER.info("Status wrote to the sheet with ID {}. Data Range: {}. Result: {}", spreadsheetId, range , result);
    }

/*    private void writePSUState(Sheets service, String spreadsheetId, String range, Map<String, String> servers) throws IOException {
        if (range.isEmpty()) {
            LOGGER.warn("PSU range is empty, please set-up `sheets.spreadsheet.psu-range` in configuration");
            return;
        }

        List<Map.Entry<String, AnalyzeResponse<PowerSupplyStatus>>> psu = DataSorter.getSortedPowerSupplies(servers);
        List<List<Object>> values = new ArrayList<>();
        for (Map.Entry<String, AnalyzeResponse<PowerSupplyStatus>> entry : psu) {
            List<Object> row = new ArrayList<>(List.of(entry.getKey(), entry.getValue().getStatus().getName().trim(), entry.getValue().getErrorText().trim()));
            values.add(row);
        }

        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        LOGGER.info("PSU wrote: {}", result);
    }

    private void writeBatteryState(Sheets service, String spreadsheetId, String range, Map<String, String> servers) throws IOException {
        if (range.isEmpty()) {
            LOGGER.warn("Battery range is empty, please set-up `sheets.spreadsheet.battery-range` in configuration");
            return;
        }

        List<Map.Entry<String, AnalyzeResponse<BatteryStatus>>> batteries = DataSorter.getSortedBatteries(servers);
        List<List<Object>> values = new ArrayList<>();
        for (Map.Entry<String, AnalyzeResponse<BatteryStatus>> entry : batteries) {
            List<Object> row = new ArrayList<>(List.of(entry.getKey(), entry.getValue().getStatus().getName().trim(), entry.getValue().getErrorText().trim()));
            values.add(row);
        }

        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        LOGGER.info("Batteries wrote: {}", result);
  }*/

    //TODO: Analyze error that trows export method
    public void removeOldCredentials() {
        Path removingPath = Path.of(null);
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
