package com.unifun.raidparser.parser;


import com.unifun.raidparser.dto.ServerInfoDTO;
import com.unifun.raidparser.loader.HostOverviewDataLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;


public class HostOverviewParser {
    private static final Logger LOGGER = LogManager.getLogger(HostOverviewParser.class);
    private static final HostOverviewDataLoader LOADER = new HostOverviewDataLoader();
    private List<ServerInfoDTO> servers = null;

    public List<ServerInfoDTO> getServers() {
        if (servers == null) {
            LOGGER.warn("No parsed servers. Start getting servers data for parsing...");
            String serversData = LOADER.getServersData();
            LOGGER.info("Got servers data. Start parsing servers data");
            servers = parseServersFromHostOverview(serversData);
        }
        return servers;
    }

    public List<ServerInfoDTO> getActualServers() {
        LOGGER.info("Getting actual servers data");
        String serversData = LOADER.getActualServersData();
        LOGGER.info("Got actual servers data. Start parsing actual servers data");
        return parseServersFromHostOverview(serversData);
    }

    private List<ServerInfoDTO> parseServersFromHostOverview(String data) {
        Document doc = Jsoup.parse(data, "UTF-8");
        Element table = doc.select("table").first();
        if (table == null) {
            LOGGER.error("Tag `table` does not existing. Cannot parsing data -> {}", doc.body());
            return new ArrayList<>();
        }

        return table.select("tr").stream()
                .map(element -> {
                    Elements innerElements = element.select("td");
                    try {
                        Element server = innerElements.getFirst();
                        Element port = innerElements.get(1);
                        Element ip = innerElements.get(2);
                        Element serverType = innerElements.get(4);

                        return buildServerInfo(server, port, ip, serverType);
                    } catch (NoSuchElementException e) {
                        LOGGER.warn("Error while getting data from row. Error -> {}", e.getMessage(), e);
                        return new ServerInfoDTO();
                    }
                })
                .filter(server -> !server.getName().isBlank())
                .toList();
    }

    private ServerInfoDTO buildServerInfo(Element server, Element port, Element ip, Element serverType) {
        String correctName = "";
        int correctPort = -1;
        String correctIp = "";
        String correctServerType = "";
        String correctConnectionType = "";

        String[] serverData = server.text().split(" ");
        if (serverData.length == 2) {
            correctName = serverData[0].trim();
            correctConnectionType = serverData[1].trim();
        } else if (serverData.length == 1) {
            correctName = serverData[0].trim();
        } else {
            LOGGER.warn("Empty server name. Element info -> {}", server.data());
        }

        if (port.text().matches("^[1-9]\\d*$")) {
            correctPort = Integer.parseInt(port.text());
        } else {
            LOGGER.warn("Port value is not an integer number. Server -> {}, Port -> {}", correctName, port.text());
        }

        correctIp = ip.text();
        correctServerType = serverType.text();

        LOGGER.debug("Creating server with name -> {}, port -> {}, ip -> {}, type -> {}, connection -> {}",
                correctName, correctPort, correctIp, correctServerType, correctConnectionType);

        return new ServerInfoDTO(correctName, correctPort, correctIp, correctServerType, correctConnectionType);
    }
}


