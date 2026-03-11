package com.unifun.raidparser.analyzer;

import com.unifun.raidparser.analyzer.filters.driver.*;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;
import com.unifun.raidparser.handlers.FileDataHandler;

import java.util.List;

public class DriveAnalyzer implements Analyzer<DriverStatus> {
    private final List<DriveFilter> driveFilters = List.of(new DriverInterimRecoveryModeFilter(),
            new DriveFailedFilter(),
            new DriverPredictiveFailureFilter(),
            new DriveEmptyFilter(),
            new DriveOkFilter()
    );

    public AnalyzeResponse<DriverStatus> analyze(String serverData) {
        String mainText = FileDataHandler.getMainData(serverData,
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
