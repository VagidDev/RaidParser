package com.unifun.components.dto;

public class ServerInfoDTO {
    private String name;
    private int port;
    private String ip;
    private String serverType;
    private String connectionType;

    public ServerInfoDTO(String name, int port, String ip, String serverType, String connectionType) {
        this.name = name;
        this.port = port;
        this.ip = ip;
        this.serverType = serverType;
        this.connectionType = connectionType;
    }

    public ServerInfoDTO() {
        this.name = "";
        this.port = -1;
        this.ip = "";
        this.serverType = "";
        this.connectionType = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
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
