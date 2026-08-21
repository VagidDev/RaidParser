package com.unifun.raidparser.controllers;

import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.controllers.dto.FileExportResponse;
import com.unifun.raidparser.controllers.dto.SheetsExportResponse;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.service.StatusExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Выгрузка статусов из кэша в статус-файлы и Google Sheets. */
@RestController
@Profile(Profiles.SERVER)
@RequestMapping(ApiPaths.BASE + "/export")
@RequiredArgsConstructor
public class ExportController {

    private final StatusExportService statusExportService;

    @PostMapping("/files")
    public FileExportResponse exportToFiles() {
        Map<HealthType, Path> files = statusExportService.exportToFiles();
        Map<HealthType, String> paths = new LinkedHashMap<>();
        files.forEach((healthType, path) -> paths.put(healthType, path.toString()));
        return new FileExportResponse(paths);
    }

    /**
     * Если выгрузился не весь набор компонентов, отвечаем 502: причина
     * (не настроен spreadsheet-id, ошибка авторизации) видна в логе.
     */
    @PostMapping("/sheets")
    public ResponseEntity<SheetsExportResponse> exportToSheets() {
        Map<HealthType, Boolean> exported = statusExportService.exportToSheets();
        boolean success = exported.values().stream().allMatch(Boolean::booleanValue);

        return ResponseEntity
                .status(success ? HttpStatus.OK : HttpStatus.BAD_GATEWAY)
                .body(new SheetsExportResponse(success, exported));
    }
}
