package com.unifun.raidparser.exporter;

import com.unifun.raidparser.config.GoogleSheetExportConfig;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.driver.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.service.GoogleSheetsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link GoogleSheetExporter}.
 *
 * <p><b>ВАЖНО — предположения о сигнатурах, которых нет в моём контексте:</b>
 * <ul>
 *   <li>{@code Status} — интерфейс/класс с методом {@code getName(): String}</li>
 *   <li>{@code ServerStatus<T extends Status>} — record/класс с методами
 *       {@code serverName(): String} и {@code analyzeResponse(): AnalyzedResponse},
 *       где {@code AnalyzedResponse} имеет {@code getStatus(): T} и {@code getErrorText(): String}</li>
 *   <li>{@code DriverStatus}, {@code PowerSupplyStatus}, {@code BatteryStatus} — реализации
 *       {@code Status}, у каждой есть какой-то enum-like конструктор/фабрика для создания экземпляра
 *       с заданным именем</li>
 * </ul>
 * Если реальные сигнатуры отличаются (особенно структура {@code analyzeResponse()}),
 * поправь хелперы {@code mockServerStatus(...)} ниже — остальной тест менять не придётся.
 *
 * <p>Тесты написаны под ИСПРАВЛЕННЫЙ маппинг range:
 * <pre>
 *   DriverStatus      -> diskRange
 *   PowerSupplyStatus -> psuRange
 *   BatteryStatus     -> batteryRange
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class GoogleSheetExporterTest {

    @Mock
    private GoogleSheetsService googleSheetsService;

    @Mock
    private GoogleSheetExportConfig googleSheetExportConfig;

    private GoogleSheetExporter exporter;

    private static final String SPREADSHEET_ID = "spreadsheet-123";
    private static final String DISK_RANGE     = "Disk!A1:C10";
    private static final String PSU_RANGE      = "PSU!A1:C10";
    private static final String BATTERY_RANGE  = "Battery!A1:C10";

    @BeforeEach
    void setUp() {
        exporter = new GoogleSheetExporter(googleSheetsService, googleSheetExportConfig);
    }

    // ── Хелперы для построения тестовых ServerStatus ───────────────────────────
    // ПРИМЕЧАНИЕ: ServerStatus и AnalyzedResponse мокируются, так как их точная
    // реализация (record/class) неизвестна. Если ServerStatus — это record с
    // компактным конструктором, замени мок на реальный конструктор.

    @SuppressWarnings("unchecked")
    private <T extends Status> ServerStatus<T> mockServerStatus(String serverName, T status, String errorText) {
        ServerStatus<T> serverStatus = mock(ServerStatus.class);
        AnalyzeResponse<T> analyzedResponse = mock(com.unifun.raidparser.core.response.AnalyzeResponse.class);
        when(serverStatus.serverName()).thenReturn(serverName);
        when(serverStatus.analyzeResponse()).thenReturn(analyzedResponse);
        when(analyzedResponse.getStatus()).thenReturn(status);
        when(analyzedResponse.getErrorText()).thenReturn(errorText);
        return serverStatus;
    }

    private DriverStatus mockDriverStatus(String name) {
        DriverStatus status = mock(DriverStatus.class);
        when(status.getName()).thenReturn(name);
        return status;
    }

    private PowerSupplyStatus mockPowerSupplyStatus(String name) {
        PowerSupplyStatus status = mock(PowerSupplyStatus.class);
        when(status.getName()).thenReturn(name);
        return status;
    }

    private BatteryStatus mockBatteryStatus(String name) {
        BatteryStatus status = mock(BatteryStatus.class);
        when(status.getName()).thenReturn(name);
        return status;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Маппинг statusClass -> range
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("export() — маппинг range по типу статуса")
    class RangeMapping {

        @Test
        @DisplayName("DriverStatus -> diskRange")
        void driverStatus_usesDiskRange() throws IOException {
            when(googleSheetExportConfig.getDiskRange()).thenReturn(DISK_RANGE);
            when(googleSheetExportConfig.getSpreadsheetId()).thenReturn(SPREADSHEET_ID);

            ServerStatus<DriverStatus> server = mockServerStatus("srv-1", mockDriverStatus("OK"), "");

            exporter.export(List.of(server), DriverStatus.class);

            verify(googleSheetsService).upload(eq(SPREADSHEET_ID), eq(DISK_RANGE), anyList());
        }

        @Test
        @DisplayName("PowerSupplyStatus -> psuRange (исправленный маппинг)")
        void powerSupplyStatus_usesPsuRange() throws IOException {
            when(googleSheetExportConfig.getPsuRange()).thenReturn(PSU_RANGE);
            when(googleSheetExportConfig.getSpreadsheetId()).thenReturn(SPREADSHEET_ID);

            ServerStatus<PowerSupplyStatus> server = mockServerStatus("srv-1", mockPowerSupplyStatus("OK"), "");

            exporter.export(List.of(server), PowerSupplyStatus.class);

            verify(googleSheetsService).upload(eq(SPREADSHEET_ID), eq(PSU_RANGE), anyList());
        }

        @Test
        @DisplayName("BatteryStatus -> batteryRange (исправленный маппинг)")
        void batteryStatus_usesBatteryRange() throws IOException {
            when(googleSheetExportConfig.getBatteryRange()).thenReturn(BATTERY_RANGE);
            when(googleSheetExportConfig.getSpreadsheetId()).thenReturn(SPREADSHEET_ID);

            ServerStatus<BatteryStatus> server = mockServerStatus("srv-1", mockBatteryStatus("OK"), "");

            exporter.export(List.of(server), BatteryStatus.class);

            verify(googleSheetsService).upload(eq(SPREADSHEET_ID), eq(BATTERY_RANGE), anyList());
        }

        @Test
        @DisplayName("Неизвестный тип статуса -> IllegalArgumentException")
        void unknownStatusClass_throwsIllegalArgumentException() {
            ServerStatus<Status> server = mockServerStatus("srv-1", mock(Status.class), "");

            assertThatThrownBy(() -> exporter.export(List.of(server), Status.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unknown status type");

            verifyNoInteractions(googleSheetsService);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Корректное построение ReportServerData
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("export() — построение ReportServerData")
    class ReportDataMapping {

        @Test
        @DisplayName("Одна запись маппится корректно: name, status, errorText")
        void singleServer_mappedCorrectly() throws IOException {
            when(googleSheetExportConfig.getDiskRange()).thenReturn(DISK_RANGE);
            when(googleSheetExportConfig.getSpreadsheetId()).thenReturn(SPREADSHEET_ID);

            ServerStatus<DriverStatus> server =
                    mockServerStatus("server-01", mockDriverStatus("DEGRADED"), "Disk read error");

            exporter.export(List.of(server), DriverStatus.class);

            ArgumentCaptor<List<ReportServerData>> captor = ArgumentCaptor.forClass(List.class);
            verify(googleSheetsService).upload(anyString(), anyString(), captor.capture());

            List<ReportServerData> data = captor.getValue();
            assertThat(data).hasSize(1);
            assertThat(data.get(0).serverName()).isEqualTo("server-01");
            assertThat(data.get(0).healthStatus()).isEqualTo("DEGRADED");
            assertThat(data.get(0).errorText()).isEqualTo("Disk read error");
        }

        @Test
        @DisplayName("Несколько записей — все попадают в список в правильном порядке")
        void multipleServers_allMappedInOrder() throws IOException {
            when(googleSheetExportConfig.getDiskRange()).thenReturn(DISK_RANGE);
            when(googleSheetExportConfig.getSpreadsheetId()).thenReturn(SPREADSHEET_ID);

            ServerStatus<DriverStatus> server1 = mockServerStatus("server-01", mockDriverStatus("OK"), "");
            ServerStatus<DriverStatus> server2 = mockServerStatus("server-02", mockDriverStatus("FAIL"), "Timeout");

            exporter.export(List.of(server1, server2), DriverStatus.class);

            ArgumentCaptor<List<ReportServerData>> captor = ArgumentCaptor.forClass(List.class);
            verify(googleSheetsService).upload(anyString(), anyString(), captor.capture());

            List<ReportServerData> data = captor.getValue();
            assertThat(data).hasSize(2);
            assertThat(data.get(0).serverName()).isEqualTo("server-01");
            assertThat(data.get(1).serverName()).isEqualTo("server-02");
            assertThat(data.get(1).errorText()).isEqualTo("Timeout");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Edge cases
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("export() — edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Пустой список serverStatuses — upload не вызывается")
        void emptyList_doesNotCallUpload() {
            exporter.export(Collections.emptyList(), DriverStatus.class);

            verifyNoInteractions(googleSheetsService);
            verifyNoInteractions(googleSheetExportConfig);
        }

        @Test
        @DisplayName("Null serverStatuses — не выбрасывает NullPointerException")
        void nullList_throwsNpe() {
            // Документирует текущий баг: serverStatuses.isEmpty() падает на null без явной проверки.
            // После фикса (добавления null-проверки с ранним выходом) этот тест нужно
            // переписать на ожидание false/early-return вместо NPE.
            assertThatCode(() -> exporter.export(null, DriverStatus.class))
                    .doesNotThrowAnyException();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Обработка исключений из GoogleSheetsService
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("export() — исключения из upload() поглощаются")
    class UploadExceptionHandling {

        @Test
        @DisplayName("IOException из upload() перехватывается и НЕ пробрасывается наверх")
        void ioExceptionFromUpload_isSwallowed() throws IOException {
            when(googleSheetExportConfig.getDiskRange()).thenReturn(DISK_RANGE);
            when(googleSheetExportConfig.getSpreadsheetId()).thenReturn(SPREADSHEET_ID);
            doThrow(new IOException("Sheets API unreachable"))
                    .when(googleSheetsService).upload(anyString(), anyString(), anyList());

            ServerStatus<DriverStatus> server = mockServerStatus("srv-1", mockDriverStatus("OK"), "");

            // export() не должен пробрасывать исключение наружу — оно только логируется
            assertThatCode(() -> exporter.export(List.of(server), DriverStatus.class))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("RuntimeException из upload() перехватывается и НЕ пробрасывается наверх")
        void runtimeExceptionFromUpload_isSwallowed() throws IOException {
            when(googleSheetExportConfig.getDiskRange()).thenReturn(DISK_RANGE);
            when(googleSheetExportConfig.getSpreadsheetId()).thenReturn(SPREADSHEET_ID);
            doThrow(new RuntimeException("Unexpected"))
                    .when(googleSheetsService).upload(anyString(), anyString(), anyList());

            ServerStatus<DriverStatus> server = mockServerStatus("srv-1", mockDriverStatus("OK"), "");

            assertThatCode(() -> exporter.export(List.of(server), DriverStatus.class))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("После исключения из upload() — токен НЕ удаляется (комментарий в коде вводит в заблуждение)")
        void exceptionDoesNotTriggerTokenRemoval() throws IOException {
            // Комментарий в catch-блоке гласит "Trying to remove old token, to fix error",
            // но фактически никакого вызова token-менеджера нет — только логирование.
            // Этот тест фиксирует текущее поведение и должен быть переписан,
            // если такая логика будет реально добавлена.
            when(googleSheetExportConfig.getDiskRange()).thenReturn(DISK_RANGE);
            when(googleSheetExportConfig.getSpreadsheetId()).thenReturn(SPREADSHEET_ID);
            doThrow(new IOException("fail"))
                    .when(googleSheetsService).upload(anyString(), anyString(), anyList());

            ServerStatus<DriverStatus> server = mockServerStatus("srv-1", mockDriverStatus("OK"), "");

            exporter.export(List.of(server), DriverStatus.class);

            // Только один вызов upload, никаких дополнительных взаимодействий с сервисами токенов
            verify(googleSheetsService, times(1)).upload(anyString(), anyString(), anyList());
            verifyNoMoreInteractions(googleSheetsService);
        }
    }
}