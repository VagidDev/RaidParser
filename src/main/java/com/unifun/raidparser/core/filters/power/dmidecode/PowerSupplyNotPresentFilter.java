package com.unifun.raidparser.core.filters.power.dmidecode;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.core.vendors.Dmidecode;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

@Component
@Dmidecode
public class PowerSupplyNotPresentFilter extends AbstractFilter<PowerSupplyStatus> {
    @Override
    public boolean filter(String text) {
        return TextSearcher.containsAll(text, "power supply not present");
    }

    @Override
    public AnalyzeResponse<PowerSupplyStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                PowerSupplyStatus.NOT_PRESENT,
                buildErrorText(text, "power supply not present", "Power supply #")
        );
    }
}
