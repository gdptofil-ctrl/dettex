/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.screens.mainmenu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

final class RandomNicknamePool {
    private static final String[] STARTS = new String[]{"X", "Thunder", "Ash", "Ayu", "Blaze", "Cry", "Dusk", "Echo", "Fae", "Frost", "Ghost", "Hex", "Ivy", "Jade", "Kiro", "Luna", "Mist", "Nova", "Onyx", "Pyro", "Raven", "Sky", "Twi", "Void", "Zen"};
    private static final String[] CORES = new String[]{"a", "storm", "bri", "cha", "dri", "e", "fa", "glo", "ka", "li", "mo", "na", "ori", "pra", "qui", "ra", "shi", "tor", "vex", "wy"};
    private static final String[] ENDS = new String[]{"x", "on", "ix", "ar", "en", "is", "or", "us", "yn", "el", "ia", "os", "um", "er", "ax", "ez", "in", "ox", "ry", "zen"};
    private static final List<String> NAMES = RandomNicknamePool.buildNames();

    private RandomNicknamePool() {
    }

    static String next(Set<String> usedNames, List<String> accounts) {
        HashSet<String> unavailable = new HashSet<String>();
        for (String used : usedNames) {
            unavailable.add(used.toLowerCase(Locale.ROOT));
        }
        for (String account : accounts) {
            unavailable.add(account.toLowerCase(Locale.ROOT));
        }
        int start = ThreadLocalRandom.current().nextInt(NAMES.size());
        for (int offset = 0; offset < NAMES.size(); ++offset) {
            String candidate = NAMES.get((start + offset) % NAMES.size());
            if (unavailable.contains(candidate.toLowerCase(Locale.ROOT))) continue;
            return candidate;
        }
        return null;
    }

    static int size() {
        return NAMES.size();
    }

    private static List<String> buildNames() {
        ArrayList<String> names = new ArrayList<String>(10000);
        HashSet<String> unique = new HashSet<String>(10000);
        for (String start : STARTS) {
            for (String core : CORES) {
                for (String end : ENDS) {
                    String name = start + core + end;
                    if (name.length() < 3 || name.length() > 15 || !name.matches("[A-Za-z]+")) {
                        throw new IllegalStateException("Invalid generated nickname: " + name);
                    }
                    if (!unique.add(name.toLowerCase(Locale.ROOT))) {
                        throw new IllegalStateException("Duplicate generated nickname: " + name);
                    }
                    names.add(name);
                }
            }
        }
        if (names.size() != 10000) {
            throw new IllegalStateException("Expected 10000 nicknames, got " + names.size());
        }
        return List.copyOf(names);
    }
}

