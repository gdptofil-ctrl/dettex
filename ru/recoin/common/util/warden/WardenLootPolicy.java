/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.warden;

public final class WardenLootPolicy {
    private WardenLootPolicy() {
    }

    public static int chestTier(long remaining, double distanceSquared) {
        if (remaining <= 0L && distanceSquared <= 25.0) {
            return 0;
        }
        if (remaining > 0L && remaining <= 5000L && distanceSquared <= 144.0) {
            return 1;
        }
        if (remaining <= 0L && distanceSquared <= 144.0) {
            return 2;
        }
        if (remaining > 0L && remaining <= 15000L && distanceSquared <= 625.0) {
            return 3;
        }
        if (remaining <= 0L) {
            return 4;
        }
        return remaining < 45000L ? 5 : -1;
    }

    public static int depositThreshold(String priority, boolean inPvp) {
        int base;
        int n = base = inPvp ? 20 : 8;
        double multiplier = "\u041d\u0438\u0437\u043a\u0438\u0439".equals(priority) ? 1.5 : ("\u0412\u044b\u0441\u043e\u043a\u0438\u0439".equals(priority) ? 0.8 : 1.0);
        return (int)((double)base * multiplier);
    }
}

