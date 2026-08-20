package com.unifun.raidparser.util;

/**
 * Поиск по выводу утилит: регистронезависимый, без учёта переводов строк.
 * Реализован через indexOf, а не через text.matches(): регулярка
 * `(?is).*\Q..\E.*` на многострочном выводе давала лишние проходы по строке
 * для каждого слова каждого фильтра.
 */
public final class TextSearcher {

    private TextSearcher() {
    }

    public static boolean containsAll(String text, String... words) {
        if (text == null || words == null || words.length == 0) {
            return false;
        }

        String lowerCaseText = text.toLowerCase();
        for (String word : words) {
            if (word == null || !lowerCaseText.contains(word.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAny(String text, String... words) {
        if (text == null || words == null || words.length == 0) {
            return false;
        }

        String lowerCaseText = text.toLowerCase();
        for (String word : words) {
            if (word != null && lowerCaseText.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Все слова должны встретиться в пределах одной строки.
     * Нужен там, где поиск по всему блоку даёт ложные срабатывания:
     * например, слово `disabled` в описании одного датчика не должно
     * менять статус блока питания целиком.
     */
    public static boolean anyLineContainsAll(String text, String... words) {
        if (text == null || words == null || words.length == 0) {
            return false;
        }
        return text.lines().anyMatch(line -> containsAll(line, words));
    }
}
