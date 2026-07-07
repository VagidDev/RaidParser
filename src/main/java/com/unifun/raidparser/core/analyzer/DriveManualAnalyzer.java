package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.drive.*;
import com.unifun.raidparser.core.filters.drive.mdadm.DriveDegradedFilter;
import com.unifun.raidparser.core.filters.drive.mdadm.DriveOkFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DriveManualAnalyzer extends AbstractAnalyzer<DriverStatus> {
    private static final List<Filter<DriverStatus>> FILTERS = List.of(
            new DriveDegradedFilter(),
            new DriveOkFilter()
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
    public HealthType getSupportedType() {
        return HealthType.DRIVE_HEALTH;
    }
}
