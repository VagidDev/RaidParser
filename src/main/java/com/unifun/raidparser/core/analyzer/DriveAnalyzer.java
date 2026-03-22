package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.filters.driver.*;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.handlers.FileDataHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriveAnalyzer implements Analyzer<DriverStatus> {
    private final FileDataHandler fileDataHandler;
    private final List<DriveFilter> driveFilters = List.of(new DriverInterimRecoveryModeFilter(),
            new DriveFailedFilter(),
            new DriverPredictiveFailureFilter(),
            new DriveEmptyFilter(),
            new DriveOkFilter()
    );

    public AnalyzeResponse<DriverStatus> analyze(String serverData) {
        String mainText = fileDataHandler.getMainData(serverData,
                "=========================drive================================",
                "==========================RAM=================================");

        AnalyzeResponse<DriverStatus> response = null;

        for (DriveFilter filter : driveFilters) {
            response = filter.filter(mainText);
            if (response.getStatus() != DriverStatus.OK)
                return response;
        }
        return response;
    }

}
