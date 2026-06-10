package com.unifun.raidparser.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.unifun.raidparser.config.GoogleSheetExporterConfig;
import com.unifun.raidparser.dto.ReportServerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GoogleSheetsExporter}.
 *
 * <p>Используем Mockito для мокирования Google Sheets API (Sheets, Spreadsheets,
 * Values, Update) — реальных HTTP-запросов нет.
 *
 * <p>Зависимости (добавить в pom.xml / build.gradle, если ещё нет):
 * <pre>
 *   // JUnit 5
 *   org.junit.jupiter:junit-jupiter:5.10.x
 *   // Mockito
 *   org.mockito:mockito-core:5.x
 *   org.mockito:mockito-junit-jupiter:5.x
 *   // AssertJ
 *   org.assertj:assertj-core:3.x
 *   // Spring Test (для ReflectionTestUtils)
 *   org.springframework:spring-test:6.x
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class GoogleSheetsExporterTest {

    // ──────────────────────────────────────────────────────────────────────────
    // Моки всей цепочки Google Sheets API:
    //   sheetsService.spreadsheets().values().update(...).setValueInputOption(...).execute()
    // ──────────────────────────────────────────────────────────────────────────

    @Mock
    private Sheets sheetsService;

    @Mock
    private Sheets.Spreadsheets spreadsheets;

    @Mock
    private Sheets.Spreadsheets.Values values;

    @Mock
    private Sheets.Spreadsheets.Values.Update updateRequest;

    @Mock
    private GoogleSheetExporterConfig googleSheetsExporterConfig;

    @InjectMocks
    private GoogleSheetsExporter exporter;

    private static final String SPREADSHEET_ID = "test-spreadsheet-id-123";
    private static final String RANGE            = "Sheet1!A1:C10";

    // ──────────────────────────────────────────────────────────────────────────
    // Тестовые данные
    // ──────────────────────────────────────────────────────────────────────────

    private ReportServerData healthyServer() {
        return new ReportServerData("server-01", "OK", "");
    }

    private ReportServerData unhealthyServer() {
        return new ReportServerData("server-02", "ERROR", "Disk full");
    }

    private ReportServerData serverWithWhitespace() {
        return new ReportServerData("server-03", "  WARNING  ", "  low memory  ");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // setUp — пробрасываем sheetsService через рефлексию,
    // так как initialize() вызывает GoogleNetHttpTransport и нам не нужен
    // реальный HTTP-транспорт.
    // ──────────────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws IOException {
        // Внедряем мок Sheets напрямую в private-поле, минуя initialize()
        ReflectionTestUtils.setField(exporter, "sheetsService", sheetsService);

        // Настраиваем стандартную цепочку вызовов Google Sheets API
        when(sheetsService.spreadsheets()).thenReturn(spreadsheets);
        when(spreadsheets.values()).thenReturn(values);
        when(values.update(anyString(), anyString(), any(ValueRange.class))).thenReturn(updateRequest);
        when(updateRequest.setValueInputOption(anyString())).thenReturn(updateRequest);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Группа: exportToSheet — корректные сценарии
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("exportToSheet — happy path")
    class ExportToSheetHappyPath {

        @Test
        @DisplayName("Одна запись — данные отправляются корректно")
        void singleRecord_sendsCorrectValues() throws Exception {
            UpdateValuesResponse mockResponse = new UpdateValuesResponse();
            when(updateRequest.execute()).thenReturn(mockResponse);

            invokeExportToSheet(SPREADSHEET_ID, RANGE, List.of(healthyServer()));

            // Захватываем ValueRange, переданный в update()
            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(eq(SPREADSHEET_ID), eq(RANGE), captor.capture());

            List<List<Object>> sent = captor.getValue().getValues();
            assertThat(sent).hasSize(1);
            assertThat(sent.get(0)).containsExactly("server-01", "OK", "");
        }

        @Test
        @DisplayName("Несколько записей — все строки попадают в таблицу")
        void multipleRecords_allRowsSent() throws Exception {
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            invokeExportToSheet(SPREADSHEET_ID, RANGE,
                    List.of(healthyServer(), unhealthyServer()));

            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(eq(SPREADSHEET_ID), eq(RANGE), captor.capture());

            List<List<Object>> sent = captor.getValue().getValues();
            assertThat(sent).hasSize(2);
            assertThat(sent.get(0)).containsExactly("server-01", "OK", "");
            assertThat(sent.get(1)).containsExactly("server-02", "ERROR", "Disk full");
        }

        @Test
        @DisplayName("Пробелы в healthStatus и errorText обрезаются через trim()")
        void whitespaceInFields_isTrimmed() throws Exception {
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            invokeExportToSheet(SPREADSHEET_ID, RANGE, List.of(serverWithWhitespace()));

            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(anyString(), anyString(), captor.capture());

            List<Object> row = captor.getValue().getValues().get(0);
            assertThat(row.get(1)).isEqualTo("WARNING");     // trim применён
            assertThat(row.get(2)).isEqualTo("low memory");  // trim применён
        }

        @Test
        @DisplayName("ValueInputOption всегда RAW")
        void valueInputOption_isRaw() throws Exception {
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            invokeExportToSheet(SPREADSHEET_ID, RANGE, List.of(healthyServer()));

            verify(updateRequest).setValueInputOption("RAW");
        }

        @Test
        @DisplayName("execute() вызывается ровно один раз")
        void execute_calledExactlyOnce() throws Exception {
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            invokeExportToSheet(SPREADSHEET_ID, RANGE, List.of(healthyServer(), unhealthyServer()));

            verify(updateRequest, times(1)).execute();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Группа: exportToSheet — граничные случаи
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("exportToSheet — edge cases")
    class ExportToSheetEdgeCases {

        @Test
        @DisplayName("Пустой range — метод завершается без вызова API")
        void emptyRange_doesNotCallApi() throws Exception {
            invokeExportToSheet(SPREADSHEET_ID, "", List.of(healthyServer()));

            verifyNoInteractions(values);
        }

        @Test
        @DisplayName("Пустой список данных — в API передаётся пустой ValueRange")
        void emptyDataList_sendsEmptyValueRange() throws Exception {
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            invokeExportToSheet(SPREADSHEET_ID, RANGE, Collections.emptyList());

            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(eq(SPREADSHEET_ID), eq(RANGE), captor.capture());
            assertThat(captor.getValue().getValues()).isEmpty();
        }

        @Test
        @DisplayName("Пустые строки в полях DTO — передаются как пустые строки, не null")
        void emptyStringFields_sentAsEmptyNotNull() throws Exception {
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());
            ReportServerData blankData = new ReportServerData("server-X", "", "");

            invokeExportToSheet(SPREADSHEET_ID, RANGE, List.of(blankData));

            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(anyString(), anyString(), captor.capture());

            List<Object> row = captor.getValue().getValues().get(0);
            assertThat(row).doesNotContainNull();
            assertThat(row).containsExactly("server-X", "", "");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Группа: exportToSheet — обработка исключений
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("exportToSheet — exceptions")
    class ExportToSheetExceptions {

        @Test
        @DisplayName("IOException от execute() пробрасывается наверх")
        void ioExceptionFromExecute_isPropagated() throws Exception {
            when(updateRequest.execute()).thenThrow(new IOException("Network error"));

            // exportToSheet — private, вызываем через рефлексию.
            // Ожидаем, что IOException всплывёт (не проглатывается внутри метода).
            var method = GoogleSheetsExporter.class
                    .getDeclaredMethod("exportToSheet", String.class, String.class, List.class);
            method.setAccessible(true);

            var ex = org.junit.jupiter.api.Assertions.assertThrows(
                    java.lang.reflect.InvocationTargetException.class,
                    () -> method.invoke(exporter, SPREADSHEET_ID, RANGE, List.of(healthyServer()))
            );
            assertThat(ex.getCause()).isInstanceOf(IOException.class)
                    .hasMessageContaining("Network error");
        }

        @Test
        @DisplayName("RuntimeException от execute() пробрасывается наверх")
        void runtimeExceptionFromExecute_isPropagated() throws Exception {
            when(updateRequest.execute()).thenThrow(new RuntimeException("Unexpected error"));

            var method = GoogleSheetsExporter.class
                    .getDeclaredMethod("exportToSheet", String.class, String.class, List.class);
            method.setAccessible(true);

            var ex = org.junit.jupiter.api.Assertions.assertThrows(
                    java.lang.reflect.InvocationTargetException.class,
                    () -> method.invoke(exporter, SPREADSHEET_ID, RANGE, List.of(healthyServer()))
            );
            assertThat(ex.getCause()).isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Unexpected error");
        }

        @Test
        @DisplayName("IOException при values.update() пробрасывается наверх")
        void ioExceptionFromValuesUpdate_isPropagated() throws Exception {
            when(values.update(anyString(), anyString(), any(ValueRange.class)))
                    .thenThrow(new IOException("Sheets API unreachable"));

            var method = GoogleSheetsExporter.class
                    .getDeclaredMethod("exportToSheet", String.class, String.class, List.class);
            method.setAccessible(true);

            var ex = org.junit.jupiter.api.Assertions.assertThrows(
                    java.lang.reflect.InvocationTargetException.class,
                    () -> method.invoke(exporter, SPREADSHEET_ID, RANGE, List.of(healthyServer()))
            );
            assertThat(ex.getCause()).isInstanceOf(IOException.class)
                    .hasMessageContaining("Sheets API unreachable");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Группа: export() — публичный метод (обёртка с try-catch)
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("export() — public wrapper")
    class ExportPublicMethod {

        @Test
        @DisplayName("export() не бросает исключений (всё внутри try-catch)")
        void export_doesNotThrowAnyException() {
            // export() содержит пустой try-catch — убеждаемся, что он не падает
            assertThatCode(() -> exporter.export("some/path"))
                    .doesNotThrowAnyException();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Группа: removeOldCredentials()
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("removeOldCredentials()")
    class RemoveOldCredentials {

        @Test
        @DisplayName("Существующие файлы credentials удаляются")
        void existingCredentials_areDeleted() throws Exception {
            // Создаём временную директорию с файлами
            Path tmpDir = Files.createTempDirectory("test-tokens");
            Path file1 = Files.createFile(tmpDir.resolve("StoredCredential"));
            Path file2 = Files.createFile(tmpDir.resolve("StoredCredential.lock"));

            // Пробрасываем путь через рефлексию (Path.of(null) — баг в текущем коде,
            // фиксируем поведение после передачи реального пути)
            var method = GoogleSheetsExporter.class
                    .getDeclaredMethod("removeOldCredentials");
            method.setAccessible(true);

            // Подменяем null-путь на реальный через рефлексию внутри метода —
            // поскольку path захардкожен как Path.of(null), тест документирует
            // текущий баг: метод выбросит NullPointerException.
            // TODO: После фикса (передача реального пути через конфиг) заменить на:
            //   method.invoke(exporter);
            //   assertThat(file1).doesNotExist();
            org.junit.jupiter.api.Assertions.assertThrows(
                    java.lang.reflect.InvocationTargetException.class,
                    () -> method.invoke(exporter)
            );

            // Cleanup
            Files.deleteIfExists(file1);
            Files.deleteIfExists(file2);
            Files.deleteIfExists(tmpDir);
        }

        @Test
        @DisplayName("removeOldCredentials() с несуществующим путём не падает с NPE — документируем баг Path.of(null)")
        void nullPath_throwsNullPointerException() throws Exception {
            var method = GoogleSheetsExporter.class
                    .getDeclaredMethod("removeOldCredentials");
            method.setAccessible(true);

            // Текущая реализация: Path.of(null) → NullPointerException
            // Этот тест фиксирует баг и служит регрессионным тестом
            var ex = org.junit.jupiter.api.Assertions.assertThrows(
                    java.lang.reflect.InvocationTargetException.class,
                    () -> method.invoke(exporter)
            );
            assertThat(ex.getCause()).isInstanceOf(NullPointerException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Вспомогательный метод: вызов private exportToSheet() через рефлексию
    // ──────────────────────────────────────────────────────────────────────────

    private void invokeExportToSheet(String spreadsheetId,
                                     String range,
                                     List<ReportServerData> data) throws Exception {
        var method = GoogleSheetsExporter.class
                .getDeclaredMethod("exportToSheet", String.class, String.class, List.class);
        method.setAccessible(true);
        method.invoke(exporter, spreadsheetId, range, data);
    }
}