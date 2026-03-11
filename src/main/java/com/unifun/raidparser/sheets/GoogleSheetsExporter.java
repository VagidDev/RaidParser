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
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.unifun.raidparser.Main;
import com.unifun.raidparser.analyzer.filters.Status;
import com.unifun.raidparser.analyzer.filters.battery.BatteryStatus;
import com.unifun.raidparser.analyzer.filters.driver.DriverStatus;
import com.unifun.raidparser.analyzer.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;
import com.unifun.raidparser.handlers.FileDataHandler;
import com.unifun.raidparser.config.AppConfig;
import com.unifun.raidparser.core.DataSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

//need to be adapted
public class GoogleSheetsExporter {
    public static String DEFAULT_TOKEN_DIRECTORY_PATH = "./tokens/";

    private static final Logger LOGGER = LogManager.getLogger(GoogleSheetsExporter.class);
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = getTokenDirectoryPath();
    private static String credentialsPath = "";
    private static String spreadsheetId = "";
    private static String diskRange = "";
    private static String psuRange = "";
    private static String batteryState = "";

    private static String getTokenDirectoryPath() {
        String tokenDir = AppConfig.get("sheets.token-path");
        if (tokenDir.isEmpty()) {
            return DEFAULT_TOKEN_DIRECTORY_PATH;
        }
        return tokenDir;
    }

    private static boolean initialize() {
        credentialsPath = AppConfig.get("sheets.credentials");
        spreadsheetId = AppConfig.get("sheets.spreadsheetId");
        if (spreadsheetId.isEmpty() || credentialsPath.isEmpty()) {
            Main.CONSOLE_LOGGER.info("Please double-check configuration file");
            LOGGER.error("Required fields in configuration are empty! Please setup `sheets.credentials` and `sheets.spreadsheetId`");
            return false;
        }

        diskRange = AppConfig.get("sheets.spreadsheet.disk-range");
        psuRange = AppConfig.get("sheets.spreadsheet.psu-range");
        batteryState = AppConfig.get("sheets.spreadsheet.battery-range");
        LOGGER.info("Google Credentials initialization successful");
        return true;
    }

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = Files.newInputStream(Path.of(credentialsPath), StandardOpenOption.READ);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Define required scopes for Sheets and Drive
        List<String> SCOPES = Arrays.asList(
                "https://www.googleapis.com/auth/spreadsheets",  // Scope for Sheets
                "https://www.googleapis.com/auth/drive"         // Optional: Scope for Drive
        );
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void export(String path) {
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
    private static void exportToSheet(String path) throws Exception {
        if (!initialize()) {
            return;
        }
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        Map<String, String> servers = FileDataHandler.readServerDataFromFile(path);

        //writeToSheet(service, spreadsheetId, diskRange, servers);
        writePSUState(service, spreadsheetId, psuRange, servers);
        writeBatteryState(service, spreadsheetId, batteryState, servers);
    }

    private static <T extends Status> void writeToSheet(Sheets service, String spreadsheetId, String range, List<Map.Entry<String, AnalyzeResponse<T>>> serversStatus) throws IOException {
        if (range.isEmpty()) {
            LOGGER.warn("Range is empty!");
            return;
        }

        List<List<Object>> values = new ArrayList<>();
        for (Map.Entry<String, AnalyzeResponse<T>> entry : serversStatus) {
            List<Object> row = new ArrayList<>(List.of(entry.getKey(), entry.getValue().getStatus().getName().trim(), entry.getValue().getErrorText().trim()));
            values.add(row);
        }

        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        LOGGER.info("Count of rows that has been wrote to range `{}` is: {}", range, result);
    }

    private static void writeDiskState(Sheets service, String spreadsheetId, String range, Map<String, String> servers) throws IOException {
        if (range.isEmpty()) {
            LOGGER.warn("Disk range is empty, please set-up `sheets.spreadsheet.disk-range` in configuration");
            return;
        }

        List<Map.Entry<String, AnalyzeResponse<DriverStatus>>> disks = DataSorter.getSortedDrives(servers);
        List<List<Object>> values = new ArrayList<>();
        for (Map.Entry<String, AnalyzeResponse<DriverStatus>> entry : disks) {
            List<Object> row = new ArrayList<>(List.of(entry.getKey(), entry.getValue().getStatus().getName().trim(), entry.getValue().getErrorText().trim()));
            values.add(row);
        }

        ValueRange body = new ValueRange().setValues(values);
        UpdateValuesResponse result = service.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        LOGGER.info("Disk wrote: {}", result);
    }

    private static void writePSUState(Sheets service, String spreadsheetId, String range, Map<String, String> servers) throws IOException {
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

    private static void writeBatteryState(Sheets service, String spreadsheetId, String range, Map<String, String> servers) throws IOException {
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
    }

    public static void removeOldCredentials() {
        Path removingPath = Path.of(TOKENS_DIRECTORY_PATH);
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
