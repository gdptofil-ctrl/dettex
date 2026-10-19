/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.repository.staff;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public final class StaffRegistry {
    private static final Set<String> NAMES = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

    private StaffRegistry() {
    }

    public static boolean add(String name) {
        return NAMES.add(name);
    }

    public static boolean contains(String name) {
        return name != null && NAMES.contains(name);
    }

    public static boolean remove(String name) {
        return name != null && NAMES.remove(name);
    }

    public static Set<String> names() {
        return Collections.unmodifiableSet(NAMES);
    }

    public static void clear() {
        NAMES.clear();
    }
}

