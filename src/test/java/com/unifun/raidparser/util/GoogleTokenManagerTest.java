package com.unifun.raidparser.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты для {@link GoogleTokenManager}.
 *
 * GoogleTokenManager не имеет внешних зависимостей кроме файловой системы,
 * поэтому используем @TempDir от JUnit 5 — никаких моков не нужно.
 */
class GoogleTokenManagerTest {

    private GoogleTokenManager tokenManager;

    // JUnit 5 автоматически создаёт и удаляет временную директорию
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        tokenManager = new GoogleTokenManager();
    }

    // ── Вспомогательные методы ────────────────────────────────────────────────

    /**
     * Создаёт файл токена с заданным временем создания (сдвиг от сейчас в днях).
     * Отрицательный daysAgo = файл создан в прошлом.
     */
    private Path createTokenWithAge(int daysAgo) throws IOException {
        Path token = Files.createFile(tempDir.resolve("StoredCredential"));
        Instant creationTime = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        Files.getFileAttributeView(token, BasicFileAttributeView.class)
                .setTimes(FileTime.from(creationTime), null, FileTime.from(creationTime));
        return token;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Guard clauses — некорректные входные данные
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Guard clauses — некорректный tokenDir")
    class GuardClauses {

        @Test
        @DisplayName("Null tokenDir — возвращает false")
        void nullTokenDir_returnsFalse() {
            assertThat(tokenManager.ensureActualityOfToken(null, 30)).isFalse();
        }

        @Test
        @DisplayName("Пустой tokenDir — возвращает false")
        void emptyTokenDir_returnsFalse() {
            assertThat(tokenManager.ensureActualityOfToken("", 30)).isFalse();
        }

        @Test
        @DisplayName("Пробельный tokenDir — возвращает false")
        void blankTokenDir_returnsFalse() {
            assertThat(tokenManager.ensureActualityOfToken("   ", 30)).isFalse();
        }

        @Test
        @DisplayName("Несуществующий путь — IOException перехватывается, возвращает false")
        void nonExistentDir_returnsFalse() {
            String nonExistent = tempDir.resolve("does-not-exist").toString();
            assertThat(tokenManager.ensureActualityOfToken(nonExistent, 30)).isFalse();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Токена нет — можно создать новый
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Токен отсутствует")
    class NoToken {

        @Test
        @DisplayName("Пустая директория — возвращает true (можно создать токен)")
        void emptyDirectory_returnsTrue() {
            boolean result = tokenManager.ensureActualityOfToken(tempDir.toString(), 30);
            assertThat(result).isTrue();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Токен существует и актуален
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Токен существует и актуален")
    class ValidToken {

        @Test
        @DisplayName("Токен создан сегодня — возвращает true, файл не удалён")
        void freshToken_returnsTrue_fileNotDeleted() throws IOException {
            Path token = createTokenWithAge(0);

            boolean result = tokenManager.ensureActualityOfToken(tempDir.toString(), 30);

            assertThat(result).isTrue();
            assertThat(token).exists(); // файл не тронут
        }

        @Test
        @DisplayName("Токен создан 1 день назад при lifetime=30 — актуален")
        void oneDayOldToken_withinLifetime_returnsTrue() throws IOException {
            Path token = createTokenWithAge(1);

            boolean result = tokenManager.ensureActualityOfToken(tempDir.toString(), 30);

            assertThat(result).isTrue();
            assertThat(token).exists();
        }

        @Test
        @DisplayName("Токен создан ровно (lifetime - 1) дней назад — ещё актуален")
        void tokenAtBoundary_returnsTrue() throws IOException {
            int lifetime = 30;
            Path token = createTokenWithAge(lifetime - 1); // 29 дней < 30

            boolean result = tokenManager.ensureActualityOfToken(tempDir.toString(), lifetime);

            assertThat(result).isTrue();
            assertThat(token).exists();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Токен устарел — должен быть удалён
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Токен устарел")
    class ExpiredToken {

        @Test
        @DisplayName("Токен создан ровно lifetime дней назад — удаляется, возвращает true")
        void tokenExactlyAtLifetime_isDeleted() throws IOException {
            int lifetime = 30;
            Path token = createTokenWithAge(lifetime); // 30 >= 30 → устарел

            boolean result = tokenManager.ensureActualityOfToken(tempDir.toString(), lifetime);

            assertThat(result).isTrue();
            assertThat(token).doesNotExist(); // файл удалён
        }

        @Test
        @DisplayName("Токен создан lifetime+5 дней назад — удаляется, возвращает true")
        void tokenOlderThanLifetime_isDeleted() throws IOException {
            Path token = createTokenWithAge(35);

            boolean result = tokenManager.ensureActualityOfToken(tempDir.toString(), 30);

            assertThat(result).isTrue();
            assertThat(token).doesNotExist();
        }

        @Test
        @DisplayName("После удаления устаревшего токена — директория остаётся")
        void afterDeletion_directoryStillExists() throws IOException {
            createTokenWithAge(60);

            tokenManager.ensureActualityOfToken(tempDir.toString(), 30);

            assertThat(tempDir).exists();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Граничные случаи с нулевым lifetime
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Граничные случаи — нулевой и отрицательный lifetime")
    class LifetimeBoundary {

        @Test
        @DisplayName("lifetime=0 — любой токен считается устаревшим")
        void zeroLifetime_anyTokenIsExpired() throws IOException {
            Path token = createTokenWithAge(0); // только что создан

            boolean result = tokenManager.ensureActualityOfToken(tempDir.toString(), 0);

            // 0 > 0 days between = false → токен устарел → удаляется
            assertThat(result).isTrue();
            assertThat(token).doesNotExist();
        }

        @Test
        @DisplayName("lifetime=1 — токен созданный вчера устаревает")
        void lifetimeOne_yesterdayTokenExpires() throws IOException {
            Path token = createTokenWithAge(1);

            boolean result = tokenManager.ensureActualityOfToken(tempDir.toString(), 1);

            assertThat(result).isTrue();
            assertThat(token).doesNotExist();
        }
    }
}