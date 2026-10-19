/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 */
package ru.recoin.common.util.warden;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2338;

public final class WardenTimerCache {
    private static final Map<Integer, Map<class_2338, Long>> TIMERS = new HashMap<Integer, Map<class_2338, Long>>();

    private WardenTimerCache() {
    }

    public static synchronized void update(int anarchy, class_2338 pos, long expiresAt) {
        if (pos != null && expiresAt > System.currentTimeMillis()) {
            TIMERS.computeIfAbsent(anarchy, ignored -> new HashMap()).put(pos.method_10062(), expiresAt);
        }
    }

    public static synchronized Map<class_2338, Long> snapshot(int anarchy, long now) {
        TIMERS.values().forEach(timers -> timers.entrySet().removeIf(entry -> (Long)entry.getValue() <= now));
        TIMERS.entrySet().removeIf(entry -> ((Map)entry.getValue()).isEmpty());
        return Map.copyOf(TIMERS.getOrDefault(anarchy, Map.of()));
    }
}

