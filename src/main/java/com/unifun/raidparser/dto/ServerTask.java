package com.unifun.raidparser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerTask {
    private String hostName;
    private String commandToExecute;
    private String commandOutput;
}
