package com.unifun.raidparser.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class TextSearcherTest {

    private final String TEXT = "Learn Java Programming and Spring Boot";

    // =========================================================================
    // ТЕСТЫ ДЛЯ МЕТОДА containsAll (Должны быть ВСЕ слова)
    // =========================================================================

    @Test
    void testContainsAll_Success() {
        // Проверка: регистр не важен, порядок слов не важен
        assertTrue(TextSearcher.containsAll(TEXT, "java", "PROGRAMMING", "spring"));
    }

    @Test
    void testContainsAll_OneWordMissing_ReturnsFalse() {
        // Одно слово из трех отсутствует в тексте
        assertFalse(TextSearcher.containsAll(TEXT, "java", "C++", "spring"));
    }

    @Test
    void testContainsAll_WithRegexSpecialCharacters() {
        // Проверка корректности работы экранирования (\Q...\E)
        String specialText = "Цена товара $100. Доставка +5 дней.";
        assertTrue(TextSearcher.containsAll(specialText, "$100", "+5"));
    }

    @Test
    void testContainsAll_EdgeCases_ReturnsFalse() {
        assertFalse(TextSearcher.containsAll(null, "java"));
        assertFalse(TextSearcher.containsAll(TEXT, (String[]) null));
        assertFalse(TextSearcher.containsAll(TEXT)); // Без передачи слов
    }

    @Test
    void testContainsAll_MultilineText_MatchesWordsOnDifferentLines() {
        // Регрессия: "." без DOTALL не матчит "\n", из-за чего containsAll
        // ломался на многострочном выводе (реальные данные с raid-контроллеров).
        String multilineText = "line one\nCache Status: OK\nBattery/Capacitor Status: OK\nline four";
        assertTrue(TextSearcher.containsAll(multilineText, "cache status: ok", "battery/capacitor status: ok"));
    }


    // =========================================================================
    // ТЕСТЫ ДЛЯ МЕТОДА containsAny (Хотя бы ОДНО слово)
    // =========================================================================

    @ParameterizedTest
    @CsvSource({
            "java, c++, python",       // Первое слово совпадает
            "C#, SPRING, javascript",   // Второе слово совпадает (в другом регистре)
            "go, ruby, Boot"            // Последнее слово совпадает
    })
    void testContainsAny_Success(String w1, String w2, String w3) {
        assertTrue(TextSearcher.containsAny(TEXT, w1, w2, w3));
    }

    @Test
    void testContainsAny_NoMatches_ReturnsFalse() {
        // Ни одного совпадения
        assertFalse(TextSearcher.containsAny(TEXT, "C++", "Python", "Ruby"));
    }

    @Test
    void testContainsAny_EdgeCases_ReturnsFalse() {
        assertFalse(TextSearcher.containsAny(null, "java"));
        assertFalse(TextSearcher.containsAny(TEXT, (String[]) null));
        assertFalse(TextSearcher.containsAny(TEXT)); // Без передачи слов
    }

    @Test
    void testContainsAny_MultilineText_MatchesWordOnMiddleLine() {
        String multilineText = "line one\nBoot camp\nline three";
        assertTrue(TextSearcher.containsAny(multilineText, "C++", "boot camp", "Ruby"));
    }
}
