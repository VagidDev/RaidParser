package com.unifun.raidparser.core.analyzer.drive;

import com.unifun.raidparser.core.analyzer.AbstractAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.drive.*;
import com.unifun.raidparser.core.filters.drive.mdadm.DriveDegradedFilter;
import com.unifun.raidparser.core.filters.drive.mdadm.DriveOkFilter;
import com.unifun.raidparser.core.vendors.Mdadm;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MdadmDriveAnalyzer extends AbstractAnalyzer<DriverStatus> {
    private final List<Filter<DriverStatus>> filters;

    public MdadmDriveAnalyzer(@Mdadm List<Filter<DriverStatus>> filters) {
        this.filters = filters;
    }

    @Override
    protected List<Filter<DriverStatus>> getFilters() {
        return filters;
    }

    @Override
    public boolean isSupportedRawData(String text) {
        return TextSearcher.containsAll(text, "Personalities", "md", "raid", "active", "blocks super"); //md raid active blocks super
    }

    @Override
    public HealthType getSupportedType() {
        return HealthType.DRIVE_HEALTH;
    }

    @Override
    public DriverStatus getUnknownStatus() {
        return DriverStatus.UNKNOWN;
    }
}
