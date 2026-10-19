/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.world;

public final class GpsMarker {
    private static boolean active;
    private static int x;
    private static int z;

    public static void set(int x, int z) {
        GpsMarker.x = x;
        GpsMarker.z = z;
        active = true;
    }

    public static void clear() {
        active = false;
    }

    private GpsMarker() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isActive() {
        return active;
    }

    public static int getX() {
        return x;
    }

    public static int getZ() {
        return z;
    }
}

