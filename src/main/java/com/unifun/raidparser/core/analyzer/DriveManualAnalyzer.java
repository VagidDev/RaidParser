package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.filters.driver.*;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
