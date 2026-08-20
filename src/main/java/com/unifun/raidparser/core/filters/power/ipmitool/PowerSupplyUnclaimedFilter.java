package com.unifun.raidparser.core.filters.power.ipmitool;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.core.vendors.Ipmitool;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

@Component
@Ipmitool
public class PowerSupplyUnclaimedFilter extends AbstractFilter<PowerSupplyStatus> {
    @Override
    public boolean filter(String text) {
        // `Unspecified` — штатное значение полей в выводе ipmitool/dmidecode,
        // поэтому смотрим только строки про блоки питания.
        return text != null && text.lines().anyMatch(line ->
                TextSearcher.containsAll(line, "unspecified")
                        && TextSearcher.containsAny(line, "ps ", "power supply", "power supplies"));
    }

    @Override
    public AnalyzeResponse<PowerSupplyStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                PowerSupplyStatus.UNCLAIMED,
                buildErrorText(text, "unspecified", "power")
        );
    }
}
