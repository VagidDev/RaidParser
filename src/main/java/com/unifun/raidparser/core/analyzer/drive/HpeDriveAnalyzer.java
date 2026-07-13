package com.unifun.raidparser.core.analyzer.drive;

import com.unifun.raidparser.core.analyzer.AbstractAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.drive.*;
import com.unifun.raidparser.core.filters.drive.hpe.DriveFailedFilter;
import com.unifun.raidparser.core.filters.drive.hpe.DriveOkFilter;
import com.unifun.raidparser.core.filters.drive.hpe.DriverInterimRecoveryModeFilter;
import com.unifun.raidparser.core.filters.drive.hpe.DriverPredictiveFailureFilter;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HpeDriveAnalyzer extends AbstractAnalyzer<DriverStatus> {
    private final List<Filter<DriverStatus>> driveFilters = List.of(
            new DriverInterimRecoveryModeFilter(),
            new DriveFailedFilter(),
            new DriverPredictiveFailureFilter(),
            new DriveEmptyFilter(),
            new DriveOkFilter()
    );

    @Override
    public boolean isSupportedRawData(String text) {
        return TextSearcher.containsAll(text, "logicaldrive", "physicaldrive");
    }

    @Override
    public HealthType getSupportedType() {
        return HealthType.DRIVE_HEALTH;
    }

    @Override
    protected List<Filter<DriverStatus>> getFilters() {
        return driveFilters;
    }

    @Override
    public DriverStatus getUnknownStatus() {
        return DriverStatus.UNKNOWN;
    }

}
