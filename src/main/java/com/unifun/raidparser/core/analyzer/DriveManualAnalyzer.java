package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.component.ComponentType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.driver.*;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriveManualAnalyzer extends AbstractAnalyzer<DriverStatus> {
    private static final List<Filter<DriverStatus>> FILTERS = List.of(
            new DriveFailedManualFilter(),
            new DriveOkManualFilter()
    );

    @Override
    protected List<Filter<DriverStatus>> getFilters() {
        return FILTERS;
    }

    @Override
    protected DriverStatus getSuccessfulStatus() {
        return DriverStatus.OK;
    }

    @Override
    public ComponentType getSupportedType() {
        return ComponentType.DRIVE_HEALTH;
    }
}
