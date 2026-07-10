package com.unifun.raidparser.exporter;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.dto.ServerStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportDataMapper {
    public List<ReportServerData> map(List<ServerStatus> serverStatuses, HealthType type) {
        return serverStatuses.stream().map(serverStatus -> new ReportServerData(
                serverStatus.serverName(),
                serverStatus.healthStatusMap().get(type).getStatus().getName(),
                serverStatus.healthStatusMap().get(type).getErrorText()
        )).toList();
    }
}
