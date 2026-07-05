package com.unifun.raidparser.dto;

import com.unifun.raidparser.core.component.HealthType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostCommand {
    private String host;
    private String command;
    private HealthType type;
}
