package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.component.ComponentType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.driver.*;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.parser.ReportFileParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriveAnalyzer extends AbstractAnalyzer<DriverStatus> {
    private final List<Filter<DriverStatus>> driveFilters = List.of(new DriverInterimRecoveryModeFilter(),
            new DriveFailedFilter(),
            new DriverPredictiveFailureFilter(),
            new DriveEmptyFilter(),
            new DriveOkFilter()
    );

    @Override
    public ComponentType getSupportedType() {
        return ComponentType.DRIVE_HEALTH;
    }

    @Override
    protected List<Filter<DriverStatus>> getFilters() {
        return driveFilters;
    }

    @Override
    protected DriverStatus getSuccessfulStatus() {
        return DriverStatus.OK;
    }
}
