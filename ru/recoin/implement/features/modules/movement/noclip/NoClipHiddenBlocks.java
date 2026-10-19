/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 */
package ru.recoin.implement.features.modules.movement.noclip;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_2338;

public final class NoClipHiddenBlocks {
    public static final Set<class_2338> POSITIONS = ConcurrentHashMap.newKeySet();

    public static boolean contains(class_2338 pos) {
        return pos != null && POSITIONS.contains(pos);
    }

    private NoClipHiddenBlocks() {
    }
}

