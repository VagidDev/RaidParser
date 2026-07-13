package com.unifun.raidparser.util;

public class TextSearcher {
    public static boolean containsAll(String text, String... words) {
        if (text == null || words == null || words.length == 0) {
            return false;
        }

        for (String word : words) {
            String regex = "(?is).*\\Q" + word + "\\E.*";
            if (!text.matches(regex)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAny(String text, String... words) {
        if (text == null || words == null || words.length == 0) {
            return false;
        }

        StringBuilder sb = new StringBuilder("(?is).*(");
        for (int i = 0; i < words.length; i++) {
            sb.append("\\Q").append(words[i]).append("\\E");
            if (i < words.length - 1) {
                sb.append("|");
            }
        }
        sb.append(").*");

        return text.matches(sb.toString());
    }

}