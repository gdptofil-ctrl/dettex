/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2535
 *  net.minecraft.class_2596
 *  net.minecraft.class_2828
 *  net.minecraft.class_2842
 *  net.minecraft.class_2851
 */
package ru.recoin.implement.features.modules.player;

import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_2842;
import net.minecraft.class_2851;

public final class GodModePacketBlocker {
    private static volatile class_2535 blockedConnection;

    private GodModePacketBlocker() {
    }

    static void attach(class_2535 connection) {
        blockedConnection = connection;
    }

    static void detach(class_2535 connection) {
        if (blockedConnection == connection) {
            blockedConnection = null;
        }
    }

    public static boolean isBlocked(class_2535 connection, class_2596<?> packet) {
        class_2535 current = blockedConnection;
        return current != null && connection == current && (packet instanceof class_2828 || packet instanceof class_2851 || packet instanceof class_2842);
    }
}

