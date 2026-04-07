package com.unifun.raidparser.handlers;

import com.unifun.raidparser.dto.ServerInfo;
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

    private List<ServerInfo> serverInfoList;

    public void loadServers() {
        getActualServerData();
        LOGGER.info("Actualizing server info list. Current count of parsed servers is {}", serverInfoList.size());
    }
    //let it be
    public void clearCache() {
        serverInfoList.clear();
        LOGGER.info("Cache is cleared");
    }

    public List<ServerInfo> getServerData() {
        if (CollectionUtils.isEmpty(serverInfoList)) {
            LOGGER.info("Server info is empty. Getting server info from HostOverview");
            String data = hostOverviewDataHandler.getData();
            LOGGER.info("Got data from HostOverview");
            serverInfoList = hostOverviewParser.parse(data);
        }

        return serverInfoList;
    }

    public List<ServerInfo> getActualServerData() {
        String data = hostOverviewDataHandler.getActualData();
        LOGGER.info("Got actual data from HostOverview");
        serverInfoList = hostOverviewParser.parse(data);
        return serverInfoList;
    }
}
