/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11245
 */
package ru.recoin.common.util.render;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.class_11245;

public final class ItemGuiAlpha {
    private static final ThreadLocal<Float> CURRENT = ThreadLocal.withInitial(() -> Float.valueOf(1.0f));
    private static final Map<class_11245, Float> ALPHAS = Collections.synchronizedMap(new WeakHashMap());

    private ItemGuiAlpha() {
    }

    public static void render(float alpha, Runnable draw) {
        float previous = CURRENT.get().floatValue();
        CURRENT.set(Float.valueOf(ItemGuiAlpha.clamp(previous * alpha)));
        try {
            draw.run();
        }
        finally {
            CURRENT.set(Float.valueOf(previous));
        }
    }

    public static void capture(class_11245 state) {
        float alpha = CURRENT.get().floatValue();
        if (alpha < 0.999f) {
            ALPHAS.put(state, Float.valueOf(alpha));
        }
    }

    public static float get(class_11245 state) {
        if (state == null) {
            return 1.0f;
        }
        return ALPHAS.getOrDefault(state, Float.valueOf(1.0f)).floatValue();
    }

    public static float current() {
        return CURRENT.get().floatValue();
    }

    private static float clamp(float alpha) {
        return Math.max(0.0f, Math.min(1.0f, alpha));
    }
}

