package com.unifun.raidparser.handlers;

import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.parser.HostOverviewParser;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HostOverviewParsedDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(HostOverviewParsedDataHandler.class);

    private final HostOverviewDataHandler hostOverviewDataHandler;
    private final HostOverviewParser hostOverviewParser;

    private List<HostInformation> hostInformationList;

    public void loadServers() {
        getActualServerData();
        LOGGER.info("Actualizing server info list. Current count of parsed servers is {}", hostInformationList.size());
    }
    //let it be
    public void clearCache() {
        hostInformationList.clear();
        LOGGER.info("Cache is cleared");
    }

    public List<HostInformation> getServerData() {
        if (CollectionUtils.isEmpty(hostInformationList)) {
            LOGGER.info("Server info is empty. Getting server info from HostOverview");
            String data = hostOverviewDataHandler.getData();
            LOGGER.info("Got data from HostOverview");
            hostInformationList = hostOverviewParser.parse(data);
        }

        return hostInformationList;
    }

    public List<HostInformation> getActualServerData() {
        String data = hostOverviewDataHandler.getActualData();
        LOGGER.info("Got actual data from HostOverview");
        hostInformationList = hostOverviewParser.parse(data);
        return hostInformationList;
    }
}
