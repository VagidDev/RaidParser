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
public class PowerSupplyNotPresentFilter extends AbstractFilter<PowerSupplyStatus> {
    @Override
    public boolean filter(String text) {
        // Ищем `disabled` в строке самого блока питания: слово встречается
        // и в описаниях других датчиков, и раньше любое такое совпадение
        // перебивало статус OK для всего PSU.
        return text != null && text.lines().anyMatch(line ->
                TextSearcher.containsAll(line, "disabled")
                        && TextSearcher.containsAny(line, "ps ", "power supply", "power supplies"));
    }

    @Override
    public AnalyzeResponse<PowerSupplyStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                PowerSupplyStatus.NOT_PRESENT,
                buildErrorText(text, "Power Supply", "Disabled", "Output")
        );
    }
}
