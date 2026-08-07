package com.unifun.raidparser.core.filters.power;

import com.unifun.raidparser.core.filters.EmptyFilter;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.core.vendors.Dmidecode;
import com.unifun.raidparser.core.vendors.Ipmitool;
import org.springframework.stereotype.Component;

@Component
@Dmidecode
@Ipmitool
public class PowerSupplyEmptyFilter extends EmptyFilter<PowerSupplyStatus> {
    @Override
    public AnalyzeResponse<PowerSupplyStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(PowerSupplyStatus.EMPTY, "Empty response, please check manually");
    }
}
