package com.unifun.raidparser.parser;

import com.unifun.raidparser.dto.ServerInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HostOverviewParserTest {
    private final HostOverviewParser hostOverviewParser = new HostOverviewParser();

    private String correctHostOverviewData() {
        return "<table class=\"table format table-striped table-hover\">" +
                "<thead>" +
                "<tr>" +
                "<th>Server name</th>" +
                "<th>SSH Port</th>" +
                "<th>Q&M Ip</th>" +
                "<th>OS Name</th>" +
                "<th>Vendor</th>" +
                "<th>Virtual CPU</th>" +
                "<th>Ram</th>" +
                "</tr>" +
                "</thead><tr>" +
                "<td><img class=\"flag\" src=\"images/flags/4x3/af.svg\"></img>test-server<div class=\"badgep\">Proxy</div></td>" +
                "<td><img class=\"flag\" src=\"images/network.png\"></img>22</td>" +
                "<td><img class=\"flag\" src=\"images/icons8-karta-sieciowa-100.png\"></img>127.0.0.1</td>" +
                "<td><img class=\"flag\" src=\"images/ubuntu.svg\"></img>Ubuntu - 20.04</td>" +
                "<td><img class=\"flag\" src=\"images/cpu.png\"></img>HP - ProLiant DL360 Gen9</td>" +
                "<td><img class=\"flag\" src=\"images/icons8-microchip-96.png\"></img>24 Threads</td>" +
                "<td><img class=\"flag\" src=\"images/ram.png\"></img>31.2 Gb</td>" +
                "</tr></table>";
    }

    private String unstructuredHostOverviewData() {
        return "<table class=\"table format table-striped table-hover\">" +
                "<thead>" +
                "<tr>" +
                "<th>Server name</th>" +
                "<th>SSH Port</th>" +
                "<th>Q&M Ip</th>" +
                "<th>OS Name</th>" +
                "<th>Vendor</th>" +
                "<th>Virtual CPU</th>" +
                "<th>Ram</th>" +
                "</tr>" +
                "</thead><tr>" +
                "<td><img class=\"flag\" src=\"images/flags/4x3/af.svg\"></img>test-server<div class=\"badgep\">Proxy</div></td>" +
                //"<td><img class=\"flag\" src=\"images/network.png\"></img>22</td>" +
                //"<td><img class=\"flag\" src=\"images/icons8-karta-sieciowa-100.png\"></img>127.0.0.1</td>" +
                "<td><img class=\"flag\" src=\"images/ubuntu.svg\"></img>Ubuntu - 20.04</td>" +
                "<td><img class=\"flag\" src=\"images/cpu.png\"></img>HP - ProLiant DL360 Gen9</td>" +
                "<td><img class=\"flag\" src=\"images/icons8-microchip-96.png\"></img>24 Threads</td>" +
                "<td><img class=\"flag\" src=\"images/ram.png\"></img>31.2 Gb</td>" +
                "</tr></table>";
    }

    private String emptyValuesHostOverviewData() {
        return "<table class=\"table format table-striped table-hover\">" +
                "<thead>" +
                "<tr>" +
                "<th>Server name</th>" +
                "<th>SSH Port</th>" +
                "<th>Q&M Ip</th>" +
                "<th>OS Name</th>" +
                "<th>Vendor</th>" +
                "<th>Virtual CPU</th>" +
                "<th>Ram</th>" +
                "</tr>" +
                "</thead><tr>" +
                "<td><img class=\"flag\" src=\"images/flags/4x3/af.svg\"></img>test-server<div class=\"badgep\">Proxy</div></td>" +
                "<td><img class=\"flag\" src=\"images/network.png\"></img></td>" +
                "<td><img class=\"flag\" src=\"images/icons8-karta-sieciowa-100.png\"></img></td>" +
                "<td><img class=\"flag\" src=\"images/ubuntu.svg\"></img>Ubuntu - 20.04</td>" +
                "<td><img class=\"flag\" src=\"images/cpu.png\"></img>HP - ProLiant DL360 Gen9</td>" +
                "<td><img class=\"flag\" src=\"images/icons8-microchip-96.png\"></img>24 Threads</td>" +
                "<td><img class=\"flag\" src=\"images/ram.png\"></img>31.2 Gb</td>" +
                "</tr></table>";
    }

    @Test
    void parse_SuccessfulParseTestServer() {
        ServerInfo serverInfoToCompare =
                new ServerInfo("test-server", 22, "127.0.0.1","HP - ProLiant DL360 Gen9", "Proxy");

        List<ServerInfo> serverInfoList = hostOverviewParser.parse(correctHostOverviewData());

        assertEquals(serverInfoToCompare, serverInfoList.get(0));
    }

    @Test
    void parse_EmptyDataForParsing() {
        List<ServerInfo> serverInfoList = hostOverviewParser.parse("");
        assertTrue(serverInfoList.isEmpty());
    }

    @Test
    void parse_DoesNotParseUnstructuredData() {
        List<ServerInfo> serverInfoList = hostOverviewParser.parse(unstructuredHostOverviewData());
        assertTrue(serverInfoList.isEmpty());
    }

    @Test
    void parse_GetServerWithoutIpAndPort() {
        ServerInfo serverInfoToCompare =
                new ServerInfo("test-server", -1, "","HP - ProLiant DL360 Gen9", "Proxy");
        List<ServerInfo> serverInfoList = hostOverviewParser.parse(emptyValuesHostOverviewData());
        assertEquals(serverInfoToCompare, serverInfoList.get(0));
    }
}