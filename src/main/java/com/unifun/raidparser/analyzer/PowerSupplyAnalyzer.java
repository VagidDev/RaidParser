package com.unifun.raidparser.analyzer;

import com.unifun.raidparser.analyzer.filters.power.*;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;
import com.unifun.raidparser.handlers.FileDataHandler;

import java.util.List;

public class PowerSupplyAnalyzer implements Analyzer<PowerSupplyStatus> {
    private final List<PowerSupplyFilter> powerSupplyFilters = List.of(
                new PowerSupplyEmptyFilter(),
                new PowerSupplyFailedFilter(),
                new PowerSupplyNotPresentFilter(),
                new PowerSupplyUnclaimedFilter(),
                new PowerSupplyOkFilter()
            );

    public AnalyzeResponse<PowerSupplyStatus> analyze(String serverData) {
        // Преобразуем данные в поток строк
        String mainText = FileDataHandler.getMainData(serverData,
                "==========================PSU=================================",
                "=========================DIMM=================================");

        AnalyzeResponse<PowerSupplyStatus> response = null;
        for (PowerSupplyFilter filter : powerSupplyFilters) {
            response = filter.filter(mainText);
            if (response.getStatus() != PowerSupplyStatus.OK)
                return response;
        }

        return response;
    }
}
