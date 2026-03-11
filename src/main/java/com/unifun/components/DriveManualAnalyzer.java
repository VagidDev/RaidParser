package com.unifun.components;

import com.unifun.components.filters.driver.*;
import com.unifun.components.response.AnalyzeResponse;

import java.util.List;

public class DriveManualAnalyzer implements Analyzer<DriverStatus> {
    private static final List<DriveFilter> FILTERS = List.of(
            new DriveFailedManualFilter(),
            new DriveOkManualFilter()
    );

    @Override
    public AnalyzeResponse<DriverStatus> analyze(String data) {
        AnalyzeResponse<DriverStatus> response = new AnalyzeResponse<>(DriverStatus.OK, "");

        for (DriveFilter filter : FILTERS) {
            response = filter.filter(data);
            if (response.getStatus() != DriverStatus.OK)
                return response;
        }

        return response;
    }
}
