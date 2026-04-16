package com.unifun.raidparser.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@EqualsAndHashCode
public class HostInformation {
    private String name;
    private int port;
    private String ip;
    private String serverType;
    private String connectionType;

    public HostInformation() {
        this.name = "";
        this.port = -1;
        this.ip = "";
        this.serverType = "";
        this.connectionType = "";
    }

    public String toStringInLine() {
        return "Server name: " + name +
                "\tConnection port: " + port +
                "\tIP Address: " + ip +
                "\tServer type: " + serverType +
                "\tConnection type: " + connectionType;
    }

    @Override
    public String toString() {
        return "Server name: " + name + "\n" +
                "Connection port: " + port + "\n" +
                "IP Address: " + ip + "\n" +
                "Server type: " + serverType + "\n" +
                "Connection type: " + connectionType;
    }
}

