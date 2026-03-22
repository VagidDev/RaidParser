package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.filters.power.*;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.handlers.FileDataHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PowerSupplyAnalyzer implements Analyzer<PowerSupplyStatus> {
    private final FileDataHandler fileDataHandler;
    private final List<PowerSupplyFilter> powerSupplyFilters = List.of(
                new PowerSupplyEmptyFilter(),
                new PowerSupplyFailedFilter(),
                new PowerSupplyNotPresentFilter(),
                new PowerSupplyUnclaimedFilter(),
                new PowerSupplyOkFilter()
            );

    public AnalyzeResponse<PowerSupplyStatus> analyze(String serverData) {
        // Преобразуем данные в поток строк
        String mainText = fileDataHandler.getMainData(serverData,
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
