package com.unifun.raidparser.core.analyzer.psu;

import com.unifun.raidparser.core.analyzer.AbstractAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.power.PowerSupplyEmptyFilter;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.filters.power.dmidecode.*;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DmidecodePowerSupplyAnalyzer extends AbstractAnalyzer<PowerSupplyStatus> {
    private final List<Filter<PowerSupplyStatus>> powerSupplyFilters = List.of(
            new PowerSupplyEmptyFilter(),
            new PowerSupplyFailedFilter(),
            new PowerSupplyNotPresentFilter(),
            new PowerSupplyUnclaimedFilter(),
            new PowerSupplyOkFilter()
    );

    @Override
    public boolean isSupportedRawData(String text) {
        return TextSearcher.containsAll(text, "Power supply #", "Present", "Redundant", "Condition");
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
