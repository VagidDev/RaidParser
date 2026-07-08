package com.unifun.raidparser.core.analyzer.drive;

import com.unifun.raidparser.core.analyzer.AbstractAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.drive.*;
import com.unifun.raidparser.core.filters.drive.mdadm.DriveDegradedFilter;
import com.unifun.raidparser.core.filters.drive.mdadm.DriveOkFilter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MdadmDriveAnalyzer extends AbstractAnalyzer<DriverStatus> {
    private static final List<Filter<DriverStatus>> FILTERS = List.of(
            new DriveDegradedFilter(),
            new DriveOkFilter()
    );

    @Override
    protected List<Filter<DriverStatus>> getFilters() {
        return FILTERS;
    }

    @Override
    public boolean isSupportedRawData(String text) {
        return (text.contains("Personalities")
                && text.contains("md")
                && text.contains("raid")
                && text.contains("active")
                && text.contains("blocks super")
        ); //md raid active blocks super
    }

    @Override
    public HealthType getSupportedType() {
        return HealthType.DRIVE_HEALTH;
    }

    @Override
    protected DriverStatus getUnknownStatus() {
        return DriverStatus.UNKNOWN;
    }
}
