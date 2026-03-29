package com.unifun.raidparser.loader;

import com.unifun.raidparser.config.HostOverviewLoaderConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;

@Component
@RequiredArgsConstructor
public class HostOverviewDataLoader {
    private static final Logger LOGGER = LogManager.getLogger(HostOverviewDataLoader.class);
    private final HostOverviewLoaderConfig hostOverviewLoaderConfig;

    public String loadData() {
        LOGGER.info("Start getting session cookies");
        String cookieWithSessionId = authorize();
        LOGGER.info("Got session Cookie: {}", cookieWithSessionId);
        LOGGER.info("Loading data from site");
        return loadServersData(cookieWithSessionId);
    }

    private String authorize() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        try (HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()) {

            String formData = String.format("username=%s&password=%s&Login=Login", hostOverviewLoaderConfig.getLogin(), hostOverviewLoaderConfig.getPassword());
            LOGGER.info("Prepared form data for POST request. Form data -> {}, POST URL -> {}", formData, hostOverviewLoaderConfig.getAuthorizationLink());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostOverviewLoaderConfig.getAuthorizationLink()))
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

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostOverviewLoaderConfig.getDataLoaderLink()))
                    .header("Cookie", cookieSessionID)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            LOGGER.info("Sending request on URL `{}` with cookie `{}`", hostOverviewLoaderConfig.getDataLoaderLink(), cookieSessionID);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Got response code `{}` for data load request on URL `{}`", response.statusCode(), response.uri());
            return response.body();
        } catch (Exception e) {
            LOGGER.error("Error while loading data from site. Error -> {}", e.getMessage(), e);
            return "";
        }
    }
}
