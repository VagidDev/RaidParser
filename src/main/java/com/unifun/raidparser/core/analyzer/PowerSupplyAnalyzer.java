package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.component.ComponentType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.power.*;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.parser.ReportFileParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PowerSupplyAnalyzer extends AbstractAnalyzer<PowerSupplyStatus> {
    private final List<Filter<PowerSupplyStatus>> powerSupplyFilters = List.of(
                new PowerSupplyEmptyFilter(),
                new PowerSupplyFailedFilter(),
                new PowerSupplyNotPresentFilter(),
                new PowerSupplyUnclaimedFilter(),
                new PowerSupplyOkFilter()
            );

    @Override
    protected List<Filter<PowerSupplyStatus>> getFilters() {
        return powerSupplyFilters;
    }

    @Override
    protected PowerSupplyStatus getSuccessfulStatus() {
        return PowerSupplyStatus.OK;
    }

    @Override
    public ComponentType getSupportedType() {
        return ComponentType.PSU_HEALTH;
    }
}
