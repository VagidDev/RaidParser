package com.unifun.raidparser.core.analyzer.psu;

import com.unifun.raidparser.core.analyzer.AbstractAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.power.*;
import com.unifun.raidparser.core.filters.power.ipmitool.PowerSupplyFailedFilter;
import com.unifun.raidparser.core.filters.power.ipmitool.PowerSupplyNotPresentFilter;
import com.unifun.raidparser.core.filters.power.ipmitool.PowerSupplyOkFilter;
import com.unifun.raidparser.core.filters.power.ipmitool.PowerSupplyUnclaimedFilter;
import com.unifun.raidparser.core.vendors.Ipmitool;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IpmitoolPowerSupplyAnalyzer extends AbstractAnalyzer<PowerSupplyStatus> {
    private final List<Filter<PowerSupplyStatus>> powerSupplyFilters;

    public IpmitoolPowerSupplyAnalyzer(@Ipmitool List<Filter<PowerSupplyStatus>> powerSupplyFilters) {
        this.powerSupplyFilters = powerSupplyFilters;
    }

    @Override
    public boolean isSupportedRawData(String text) {
        return TextSearcher.containsAll(text, "Power Supply","Power Supplies");
    }

    @Override
    protected List<Filter<PowerSupplyStatus>> getFilters() {
        return powerSupplyFilters;
    }

    @Override
    public HealthType getSupportedType() {
        return HealthType.PSU_HEALTH;
    }

    @Override
    public PowerSupplyStatus getUnknownStatus() {
        return PowerSupplyStatus.UNKNOWN;
    }
}
