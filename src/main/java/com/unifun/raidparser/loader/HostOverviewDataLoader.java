package com.unifun.raidparser.loader;

import com.unifun.raidparser.config.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;

public class HostOverviewDataLoader {
    private static final Logger LOGGER = LogManager.getLogger(HostOverviewDataLoader.class);
    private static final String CACHE_FILE = "./cache/servers.tmp";
    private String user = "";
    private String password = "";
    private String authorizationLink = "";
    private String dataLoaderLink = "";

    private boolean initialize() {
        user = AppConfig.get("html.user");
        password = AppConfig.get("html.password");
        authorizationLink = AppConfig.get("html.authorization");
        dataLoaderLink = AppConfig.get("html.data-loader");

        if (user.isEmpty() || password.isEmpty() || authorizationLink.isEmpty() || dataLoaderLink.isEmpty()) {
            LOGGER.error("Failed to get configuration for loading data. Please set up values for `html.user`, `html.password`, `html.authorization` and `html.data-loader` in configuration. " +
                    "Current values: html.user -> {}, html.password -> {}, html.authorization -> {}, html.data-loader -> {}", user, password, authorizationLink, dataLoaderLink);
            return false;
        }

        return true;
    }

    private Path getCachePath() {
        Path cachePath = Paths.get(CACHE_FILE);
        if (Files.exists(cachePath)) {
            return cachePath;
        }
        LOGGER.warn("Cache file does not exists! Creating cache file {}", cachePath);
        try {
            Files.createDirectories(cachePath.getParent());
            Files.createFile(cachePath);
            LOGGER.info("Cache file {} is created!", cachePath);
            return cachePath;
        } catch (IOException e) {
            LOGGER.error("Error while creating cache file. Error -> {}", e.getMessage(), e);
            return null;
        }
    }

    public String getActualServersData() {
        Path cacheFile = getCachePath();

        if (cacheFile == null) {
            return "";
        }

        try {
            LOGGER.info("Truncating data in cache file {}", cacheFile);
            Files.writeString(cacheFile, "", StandardOpenOption.TRUNCATE_EXISTING);
            return getServersData();
        } catch (IOException e) {
            LOGGER.error("Error while truncating cache file {}. Error -> {}", cacheFile, e.getMessage(), e);
            return "";
        }
    }

    public String getServersData() {
        Path cacheFile = getCachePath();
        if (cacheFile == null) {
            return "";
        }

        String serversData = getServersDataFromFile(cacheFile);
        if (!serversData.isEmpty()) {
            return serversData;
        }
        LOGGER.warn("Empty cache. Trying to get data from API...");
        serversData = getServersDataFromApi();
        LOGGER.info("Got data from API");
        try {
            if (!serversData.isEmpty()) {
                LOGGER.info("Writing data from API to cache file {}", cacheFile);
                Files.writeString(cacheFile, serversData);
            }
        } catch (IOException e) {
            LOGGER.error("Error while tying write data from api to cache file {}. Error -> {}", cacheFile, e.getMessage(), e);
        }
        return serversData;
    }

    private String getServersDataFromFile(Path file) {
        try {
            LOGGER.info("Getting data from cache file {}", file);
            return Files.readString(file);
        } catch (IOException e) {
            LOGGER.error("Error while reading data from cache file {}. Error -> {}", file, e.getMessage(), e);
            return "";
        }
    }

    private String getServersDataFromApi() {
        if (!initialize()) {
            return "";
        }

        LOGGER.info("Start getting session cookies");
        String cookieWithSessionId = getCookies();
        LOGGER.info("Got session Cookie: {}", cookieWithSessionId);

        LOGGER.info("Loading data from site");
        return loadServersData(cookieWithSessionId);
    }

    private String getCookies() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        try (HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()) {

            String formData = String.format("username=%s&password=%s&Login=Login", user, password);
            LOGGER.info("Prepared form data for POST request. Form data -> {}, POST URL -> {}", formData, authorizationLink);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authorizationLink))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "*/*")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Got response code `{}` for authorization request on URL `{}`", response.statusCode(), response.uri());

            return cookieManager.getCookieStore()
                    .getCookies().stream()
                    .map(HttpCookie::toString)
                    .filter(cookie -> cookie.contains("PHPSESSID"))
                    .findFirst()
                    .orElse("");
        } catch (Exception e) {
            LOGGER.error("Get error while getting cookies for authorized session. Error -> {}", e.getMessage(), e);
            return "";
        }
    }

    private String loadServersData(String cookieSessionID) {
        try (HttpClient client = HttpClient.newHttpClient()) {

            // Создание запроса с параметрами авторизации
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(dataLoaderLink))
                    .header("Cookie", cookieSessionID)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            LOGGER.info("Sending request on URL `{}` with cookie `{}`", dataLoaderLink, cookieSessionID);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Got response code `{}` for data load request on URL `{}`", response.statusCode(), response.uri());
            return response.body();
        } catch (Exception e) {
            LOGGER.error("Error while loading data from site. Error -> {}", e.getMessage(), e);
            return "";
        }
    }
}
