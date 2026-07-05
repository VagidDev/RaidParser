package com.unifun.raidparser.config;

import com.unifun.raidparser.dto.HostCommand;
import lombok.Data;


import java.util.List;

@Data
public class ServerTasksConfig {
    private List<HostCommand> commands;
}
