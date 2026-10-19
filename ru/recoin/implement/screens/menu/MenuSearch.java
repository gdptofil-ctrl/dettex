/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_4587;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.implement.screens.menu.MenuStyle;

public final class MenuSearch {
    private static final String EN = "qwertyuiop[]asdfghjkl;'zxcvbnm,.`";
    private static final String RU = "\u0439\u0446\u0443\u043a\u0435\u043d\u0433\u0448\u0449\u0437\u0445\u044a\u0444\u044b\u0432\u0430\u043f\u0440\u043e\u043b\u0434\u0436\u044d\u044f\u0447\u0441\u043c\u0438\u0442\u044c\u0431\u044e\u0451";

    private MenuSearch() {
    }

    public static List<String> variants(String query) {
        String original = query.strip().toLowerCase(Locale.ROOT);
        if (original.isEmpty()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<String>();
        result.add(original);
        StringBuilder en = new StringBuilder();
        StringBuilder ru = new StringBuilder();
        for (char c : original.toCharArray()) {
            int ri = RU.indexOf(c);
            int ei = EN.indexOf(c);
            en.append(ri < 0 ? c : EN.charAt(ri));
            ru.append(ei < 0 ? c : RU.charAt(ei));
        }
        if (!result.contains(en.toString())) {
            result.add(en.toString());
        }
        if (!result.contains(ru.toString())) {
            result.add(ru.toString());
        }
        return result;
    }

    public static boolean matches(String text, String query) {
        if (query.isBlank()) {
            return true;
        }
        String haystack = text.toLowerCase(Locale.ROOT);
        for (String variant : MenuSearch.variants(query)) {
            boolean all = true;
            for (String word : variant.split("\\s+")) {
                all &= haystack.contains(word);
            }
            if (!all) continue;
            return true;
        }
        return false;
    }

    public static void draw(class_4587 matrices, FontRenderer font, String text, float x, float y, int color, String query) {
        if (query.isBlank()) {
            font.drawString(matrices, text, x, y, color);
            return;
        }
        boolean[] marked = new boolean[text.length()];
        String lower = text.toLowerCase(Locale.ROOT);
        for (String variant : MenuSearch.variants(query)) {
            for (String word : variant.split("\\s+")) {
                if (word.isEmpty()) continue;
                int i = lower.indexOf(word);
                while (i >= 0) {
                    for (int j = i; j < Math.min(marked.length, i + word.length()); ++j) {
                        marked[j] = true;
                    }
                    i = lower.indexOf(word, i + 1);
                }
            }
        }
        int start = 0;
        while (start < text.length()) {
            int end;
            for (end = start + 1; end < text.length() && marked[end] == marked[start]; ++end) {
            }
            font.drawString(matrices, text.substring(start, end), x + font.getStringWidth(text.substring(0, start)), y, marked[start] ? MenuStyle.accent() : color);
            start = end;
        }
    }
}

