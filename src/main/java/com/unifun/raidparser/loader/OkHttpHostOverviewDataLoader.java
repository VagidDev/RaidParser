package com.unifun.raidparser.loader;

import com.unifun.raidparser.config.HostOverviewLoaderConfig;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OkHttpHostOverviewDataLoader implements HostOverviewDataLoader {
    private static final Logger LOGGER = LogManager.getLogger(OkHttpHostOverviewDataLoader.class);
    private static final MediaType URL_ENCODED = MediaType.get("application/x-www-form-urlencoded");

    private final HostOverviewLoaderConfig hostOverviewLoaderConfig;

    @Override
    public String loadData() {
        LOGGER.info("Start getting session cookies");
        String cookieWithSessionId = authorize();
        LOGGER.info("Got session Cookie: {}", cookieWithSessionId);
        LOGGER.info("Loading data from site");
        //return loadServersData(cookieWithSessionId);
        return "";
    }

    private String authorize() {
        OkHttpClient client = new OkHttpClient();
        String formData = String.format("username=%s&password=%s&Login=Login", hostOverviewLoaderConfig.getLogin(), hostOverviewLoaderConfig.getPassword());

        RequestBody requestBody = RequestBody.create(formData, URL_ENCODED);

//        try (Response response = client.newCall(requestBody)) {
//
//        }
        return "";
    }

}
