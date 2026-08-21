package com.unifun.raidparser.service;

import com.unifun.raidparser.config.OutputStatusFileConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.exporter.FileExporter;
import com.unifun.raidparser.exporter.GoogleSheetExporter;
import com.unifun.raidparser.mapper.ExportDataMapper;
import com.unifun.raidparser.util.ServerStatusSorter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Экспорт статусов из кэша. Вынесен из интерактивной консоли, чтобы
 * консоль и REST API выгружали данные одним и тем же кодом.
 */
@Service
@RequiredArgsConstructor
public class StatusExportService {
    private static final Logger LOGGER = LogManager.getLogger(StatusExportService.class);

    private final RaidParserService raidParserService;
    private final ServerStatusSorter serverStatusSorter;
    private final ExportDataMapper exportDataMapper;
    private final GoogleSheetExporter googleSheetExporter;
    private final FileExporter fileExporter;
    private final OutputStatusFileConfig outputStatusFileConfig;

    /** @return файл, в который выгружен каждый тип компонента. */
    public Map<HealthType, Path> exportToFiles() {
        List<ServerStatus> cachedStatus = raidParserService.getCachedStatus();
        Map<HealthType, Path> files = new EnumMap<>(HealthType.class);

        for (HealthType healthType : HealthType.reportable()) {
            Path path = Path.of(outputFileOf(healthType));
            fileExporter.export(path, report(cachedStatus, healthType));
            files.put(healthType, path);
        }

        LOGGER.info("Exported statuses to files: {}", files.values());
        return files;
    }

    /** @return результат выгрузки по каждому типу компонента. */
    public Map<HealthType, Boolean> exportToSheets() {
        List<ServerStatus> cachedStatus = raidParserService.getCachedStatus();
        Map<HealthType, Boolean> exported = new EnumMap<>(HealthType.class);

        for (HealthType healthType : HealthType.reportable()) {
            exported.put(healthType, googleSheetExporter.export(report(cachedStatus, healthType), healthType));
        }

        LOGGER.info("Exported statuses to Google Sheets: {}", exported);
        return exported;
    }

    private List<com.unifun.raidparser.dto.ReportServerData> report(List<ServerStatus> statuses, HealthType healthType) {
        return exportDataMapper.map(serverStatusSorter.sortByHealthStatus(statuses, healthType), healthType);
    }

    private String outputFileOf(HealthType healthType) {
        return switch (healthType) {
            case DRIVE_HEALTH -> outputStatusFileConfig.getDriveStatus();
            case PSU_HEALTH -> outputStatusFileConfig.getPsuStatus();
            case BATTERY_HEALTH -> outputStatusFileConfig.getBatteryStatus();
            default -> throw new IllegalArgumentException("No output file configured for " + healthType);
        };
    }
}
