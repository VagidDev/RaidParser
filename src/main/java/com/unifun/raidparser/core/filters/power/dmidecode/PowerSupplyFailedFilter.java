package com.unifun.raidparser.core.filters.power.dmidecode;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.power.PowerSupplyFilter;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.util.TextSearcher;

public class PowerSupplyFailedFilter extends AbstractFilter<PowerSupplyStatus> implements PowerSupplyFilter {
    @Override
    public boolean filter(String text) {
        return TextSearcher.containsAny(text, "condition: failed","redundant: no");
    }

    @Override
    public AnalyzeResponse<PowerSupplyStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(PowerSupplyStatus.FAILED,
                buildErrorText(text,  "condition: failed", "power supply #", "redundant: no")
        );
    }
}
