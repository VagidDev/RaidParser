package com.unifun.raidparser.core.filters.drive.mdadm;

import com.unifun.raidparser.util.TextSearcher;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Разбор строк состояния массива из /proc/mdstat.
 * <p>
 * Формат `[всего/живых] [UU_]` одинаков для raid1, raid5, raid6 и raid10,
 * поэтому сравниваем числа, а не ищем готовые строки вида "[2/2] [UU]":
 * из-за поиска по строкам всё, кроме двухдискового raid1, попадало в UNKNOWN.
 */
final class MdstatStatusLine {
    private static final Pattern ARRAY_STATE = Pattern.compile("\\[(\\d+)/(\\d+)]\\s*\\[([U_]+)]");

    private MdstatStatusLine() {
    }

    static boolean hasAnyArray(String text) {
        return !arrayLines(text).isEmpty();
    }

    static List<String> arrayLines(String text) {
        if (text == null) {
            return List.of();
        }
        return text.lines()
                .filter(line -> ARRAY_STATE.matcher(line).find())
                .toList();
    }

    /** Строки массивов, которым не хватает дисков, плюс массивы в состоянии inactive. */
    static List<String> problemLines(String text) {
        if (text == null) {
            return List.of();
        }
        return text.lines()
                .filter(line -> isDegraded(line) || isInactive(line))
                .toList();
    }

    private static boolean isDegraded(String line) {
        Matcher matcher = ARRAY_STATE.matcher(line);
        while (matcher.find()) {
            int expected = Integer.parseInt(matcher.group(1));
            int active = Integer.parseInt(matcher.group(2));
            if (active < expected || matcher.group(3).indexOf('_') >= 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInactive(String line) {
        return TextSearcher.containsAll(line, "md", "inactive");
    }
}
