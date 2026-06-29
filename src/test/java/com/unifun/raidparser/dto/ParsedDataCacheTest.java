package com.unifun.raidparser.dto;

import com.unifun.raidparser.core.filters.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;

class ParsedDataCacheTest {

    // Простая реализация Status для тестов
    enum TestStatus implements Status {
        OK, FAIL;

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public String getName() {
            return "";
        }
    }

    private static final Path FILE_A = Path.of("/reports/file_a.txt");
    private static final Path FILE_B = Path.of("/reports/file_b.txt");
    private static final long CACHE_TTL_SECONDS = 60L;

    private ParsedDataCache<TestStatus> cache;

    @BeforeEach
    void setUp() {
        cache = new ParsedDataCache<>(CACHE_TTL_SECONDS);
    }

    // ─────────────────────────────────────────────────────────────
    // Начальное состояние
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Initial state")
    class InitialState {

        @Test
        @DisplayName("serverStatusData is empty list after construction")
        void serverStatusData_isEmptyList_afterConstruction() {
            assertThat(cache.getServerStatusData()).isEmpty();
        }

        @Test
        @DisplayName("isDataValid returns false when cache is freshly created (no data stored yet)")
        void isDataValid_returnsFalse_whenNothingStoredYet() {
            // parsedDateTime == null — не должно бросить NPE, просто false
            assertThat(cache.isDataValid(FILE_A)).isFalse();
        }

        @Test
        @DisplayName("isDataValid does NOT throw NPE when parsedDateTime is null (regression)")
        void isDataValid_doesNotThrowNpe_whenParsedDateTimeIsNull() {
            // Баг из ревью: ChronoUnit.between(null, ...) кидал NPE
            assertThatNoException().isThrownBy(() -> cache.isDataValid(FILE_A));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // store()
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("store()")
    class StoreMethod {

        @Test
        @DisplayName("store saves data and makes cache valid for the same file")
        void store_savesData_andCacheBecomesValid() {
            List<ServerStatus<TestStatus>> data = List.of(makeServerStatus());
            cache.store(data, FILE_A);

            assertThat(cache.isDataValid(FILE_A)).isTrue();
        }

        @Test
        @DisplayName("store saves parsedFromFile so subsequent isDataValid works (regression: field was never assigned)")
        void store_savesReportFilePath_soIsDataValidWorks() {
            // Баг из ревью: parsedFromFile не сохранялся → isDataValid всегда false
            List<ServerStatus<TestStatus>> data = List.of(makeServerStatus());
            cache.store(data, FILE_A);

            // Должно быть true, а не false из-за незаписанного пути
            assertThat(cache.isDataValid(FILE_A)).isTrue();
        }

        @Test
        @DisplayName("store saves data accessible via getServerStatusData")
        void store_savedData_isReturnedByGetter() {
            ServerStatus<TestStatus> status = makeServerStatus();
            List<ServerStatus<TestStatus>> data = List.of(status);
            cache.store(data, FILE_A);

            assertThat(cache.getServerStatusData()).containsExactly(status);
        }

        @Test
        @DisplayName("store with null data does nothing (no exception, cache stays invalid)")
        void store_withNullData_doesNothing() {
            assertThatNoException().isThrownBy(() -> cache.store(null, FILE_A));
            assertThat(cache.isDataValid(FILE_A)).isFalse();
        }

        @Test
        @DisplayName("store with null path does nothing (no exception, cache stays invalid)")
        void store_withNullPath_doesNothing() {
            assertThatNoException().isThrownBy(() ->
                    cache.store(List.of(makeServerStatus()), null));
            assertThat(cache.isDataValid(FILE_A)).isFalse();
        }

        @Test
        @DisplayName("store with both null args does nothing safely")
        void store_withBothNullArgs_doesNothingSafely() {
            assertThatNoException().isThrownBy(() -> cache.store(null, null));
        }

        @Test
        @DisplayName("re-store with new data replaces old data")
        void store_withNewData_replacesOldData() {
            ServerStatus<TestStatus> first = makeServerStatus();
            ServerStatus<TestStatus> second = makeServerStatus();

            cache.store(List.of(first), FILE_A);
            cache.store(List.of(second), FILE_A);

            assertThat(cache.getServerStatusData()).containsExactly(second);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // isDataValid()
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isDataValid()")
    class IsDataValid {

        @Test
        @DisplayName("returns false when report file differs from stored file")
        void isDataValid_returnsFalse_whenFileDiffers() {
            cache.store(List.of(makeServerStatus()), FILE_A);
            // Спрашиваем про FILE_B, а в кэше FILE_A
            assertThat(cache.isDataValid(FILE_B)).isFalse();
        }

        @Test
        @DisplayName("returns false when cached data is empty list")
        void isDataValid_returnsFalse_whenDataIsEmpty() {
            // store с пустым списком — валидный вызов, но данных нет
            cache.store(List.of(), FILE_A);
            assertThat(cache.isDataValid(FILE_A)).isFalse();
        }

        @Test
        @DisplayName("returns false when cache TTL has expired")
        void isDataValid_returnsFalse_whenTtlExpired() throws InterruptedException {
            // Создаём кэш с TTL = 0 секунд (моментально протухает)
            ParsedDataCache<TestStatus> shortLivedCache = new ParsedDataCache<>(0L);
            shortLivedCache.store(List.of(makeServerStatus()), FILE_A);

            // Небольшая пауза, чтобы время гарантированно прошло
            Thread.sleep(1000);

            assertThat(shortLivedCache.isDataValid(FILE_A)).isFalse();
        }

        @Test
        @DisplayName("returns true when same file, non-empty data, within TTL")
        void isDataValid_returnsTrue_whenAllConditionsMet() {
            cache.store(List.of(makeServerStatus()), FILE_A);
            assertThat(cache.isDataValid(FILE_A)).isTrue();
        }

        @Test
        @DisplayName("returns true repeatedly within TTL (multiple calls)")
        void isDataValid_returnsTrue_onMultipleCallsWithinTtl() {
            cache.store(List.of(makeServerStatus()), FILE_A);

            assertThat(cache.isDataValid(FILE_A)).isTrue();
            assertThat(cache.isDataValid(FILE_A)).isTrue();
            assertThat(cache.isDataValid(FILE_A)).isTrue();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private ServerStatus<TestStatus> makeServerStatus() {
        return mock(ServerStatus.class);
    }
}