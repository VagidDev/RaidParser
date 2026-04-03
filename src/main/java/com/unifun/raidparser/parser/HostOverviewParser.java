package com.unifun.raidparser.parser;

import com.unifun.raidparser.dto.ServerInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HostOverviewParser {
    private static final Logger LOGGER = LogManager.getLogger(HostOverviewParser.class);

    public List<ServerInfo> parse(String data) {
        Document doc = Jsoup.parse(data, "UTF-8");
        Element table = doc.select("table").first();
        if (table == null) {
            LOGGER.error("Tag `table` does not existing. Cannot parsing data -> {}", doc.body());
            return new ArrayList<>();
        }

        return table.select("tr").stream()
                .map(element -> {
                    Elements innerElements = element.select("td");

                    if (innerElements.size() != 7) {
                        LOGGER.warn("Skip element {} due to the number of elements is not equal to `7`", element);
                        return new ServerInfo();
                    }

                    try {
                        LOGGER.debug("Parsing inner element {}", innerElements);
                        Element server = innerElements.get(0);
                        Element port = innerElements.get(1);
                        Element ip = innerElements.get(2);
                        Element serverType = innerElements.get(4);

                        return buildServerInfo(server, port, ip, serverType);
                    } catch (NoSuchElementException e) {
                        LOGGER.warn("Error while getting data from row. Error -> {}", e.getMessage(), e);
                        return new ServerInfo();
                    }
                })
                .filter(server -> !server.getName().isBlank())
                .toList();
    }

    private ServerInfo buildServerInfo(Element server, Element port, Element ip, Element serverType) {
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

        return new ServerInfo(correctName, correctPort, correctIp, correctServerType, correctConnectionType);
    }
}


