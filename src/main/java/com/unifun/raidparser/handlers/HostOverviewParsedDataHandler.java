package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.CacheConfig;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.parser.HostOverviewParser;
import com.unifun.raidparser.util.CacheExpiry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.util.List;

@Service
public class HostOverviewParsedDataHandler implements ManagedCache {
    private static final Logger LOGGER = LogManager.getLogger(HostOverviewParsedDataHandler.class);
    private static final String CACHE_NAME = "hosts";

    private final HostOverviewDataHandler hostOverviewDataHandler;
    private final HostOverviewParser hostOverviewParser;
    private final CacheExpiry expiry;

    private List<HostInformation> hostInformationList = List.of();

    public HostOverviewParsedDataHandler(HostOverviewDataHandler hostOverviewDataHandler,
                                         HostOverviewParser hostOverviewParser,
                                         Clock clock,
                                         CacheConfig cacheConfig) {
        this.hostOverviewDataHandler = hostOverviewDataHandler;
        this.hostOverviewParser = hostOverviewParser;
        this.expiry = new CacheExpiry(clock, cacheConfig.getHostOverviewTtlSeconds());
    }

    public void loadServers() {
        getActualServerData();
        LOGGER.info("Actualizing server info list. Current count of parsed servers is {}", hostInformationList.size());
    }

    public List<HostInformation> getServerData() {
        if (CollectionUtils.isEmpty(hostInformationList)) {
            LOGGER.info("Server info is empty. Getting server info from HostOverview");
            return reload(hostOverviewDataHandler.getData());
        }

        if (expiry.isExpired()) {
            LOGGER.info("Server info is older than {} seconds, reloading it from HostOverview", expiry.ttlSeconds());
            return getActualServerData();
        }

        return hostInformationList;
    }

    public List<HostInformation> getActualServerData() {
        List<HostInformation> servers = reload(hostOverviewDataHandler.getActualData());
        LOGGER.info("Got actual data from HostOverview");
        return servers;
    }

    @Override
    public String cacheName() {
        return CACHE_NAME;
    }

    @Override
    public void clear() {
        // parse() возвращает неизменяемый список, поэтому именно замена, а не clear()
        hostInformationList = List.of();
        expiry.invalidate();
        LOGGER.info("Host list cache is cleared");
    }

    @Override
    public CacheState state() {
        return new CacheState(
                CACHE_NAME,
                !hostInformationList.isEmpty(),
                hostInformationList.size(),
                expiry.age(),
                expiry.ttlSeconds(),
                expiry.isExpired()
        );
    }

    private List<HostInformation> reload(String data) {
        hostInformationList = hostOverviewParser.parse(data);
        expiry.markLoaded();
        return hostInformationList;
    }
}
