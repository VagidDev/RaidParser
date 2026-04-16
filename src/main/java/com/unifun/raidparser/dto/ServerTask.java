package com.unifun.raidparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerTask {
    private String hostName;
    private String commandToExecute;
    private String commandOutput;
}
