package com.unifun.raidparser.core.analyzer.drive;

import com.unifun.raidparser.core.analyzer.AbstractAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.drive.*;
import com.unifun.raidparser.core.filters.drive.ssacli.DriveFailedFilter;
import com.unifun.raidparser.core.filters.drive.ssacli.DriveOkFilter;
import com.unifun.raidparser.core.filters.drive.ssacli.DriverInterimRecoveryModeFilter;
import com.unifun.raidparser.core.filters.drive.ssacli.DriverPredictiveFailureFilter;
import com.unifun.raidparser.core.vendors.Ssacli;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SsacliDriveAnalyzer extends AbstractAnalyzer<DriverStatus> {
    private final List<Filter<DriverStatus>> driveFilters;

    public SsacliDriveAnalyzer(@Ssacli List<Filter<DriverStatus>> driveFilters) {
        this.driveFilters = driveFilters;
    }

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
