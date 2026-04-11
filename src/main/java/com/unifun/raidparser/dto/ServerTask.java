package com.unifun.raidparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerTask {
    private String serverName;
    private String commandToExecute;
}
