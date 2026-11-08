package ru.recoin.implement.features.modules.render.hitcolor;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3489;

/** Remembers which entities the local player hit, when, and whether it was with a sword. */
public final class HitTracker {
    private record Hit(long at, boolean sword) {
    }

    private static final Map<Integer, Hit> HITS = new HashMap<>();
    /** Tint of the armor being drawn right now (set around the armor feature renderer), 0 = none. */
    public static int armorTint;

    private HitTracker() {
    }

    public static synchronized void onAttack(class_1297 target) {
        if (target == null) {
            return;
        }
        boolean sword = false;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 != null) {
            class_1799 stack = mc.field_1724.method_6047();
            sword = stack != null && !stack.method_7960() && stack.method_31573(class_3489.field_42611);
        }
        HITS.put(target.method_5628(), new Hit(System.currentTimeMillis(), sword));
        if (HITS.size() > 256) {
            long now = System.currentTimeMillis();
            HITS.values().removeIf(h -> now - h.at > 5000L);
        }
    }

    /**
     * Strength of the hit effect 0..1 (1 right after the hit, fading out over durationMs), or 0.
     * weapon: "Any", "Sword", "No Sword"; onlyMine: false = any damage (the entity's hurt time).
     */
    public static synchronized float strength(class_1297 entity, String weapon, boolean onlyMine, long durationMs) {
        if (entity == null) {
            return 0.0f;
        }
        if (onlyMine) {
            Hit hit = HITS.get(entity.method_5628());
            if (hit == null) return 0.0f;
            if ("Sword".equals(weapon) && !hit.sword || "No Sword".equals(weapon) && hit.sword) return 0.0f;
            long age = System.currentTimeMillis() - hit.at;
            if (age >= durationMs) return 0.0f;
            return 1.0f - age / (float)durationMs;
        }
        if (entity instanceof class_1309 living && living.field_6235 > 0) {
            Hit hit = HITS.get(entity.method_5628());
            boolean sword = hit != null && System.currentTimeMillis() - hit.at < 1000L && hit.sword;
            if ("Sword".equals(weapon) && !sword || "No Sword".equals(weapon) && sword) return 0.0f;
            return living.field_6235 / (float)Math.max(1, living.field_6254);
        }
        return 0.0f;
    }

    /** Multiplies two ARGB colors (used to tint a model). */
    public static int multiply(int a, int b) {
        int aa = (a >>> 24) * (b >>> 24) / 255;
        int r = (a >> 16 & 0xFF) * (b >> 16 & 0xFF) / 255;
        int g = (a >> 8 & 0xFF) * (b >> 8 & 0xFF) / 255;
        int bl = (a & 0xFF) * (b & 0xFF) / 255;
        return aa << 24 | r << 16 | g << 8 | bl;
    }

    /** White blended towards color by amount (0..1), fully opaque: a multiplicative tint. */
    public static int tint(int color, float amount) {
        amount = Math.max(0.0f, Math.min(1.0f, amount));
        int r = Math.round(255 + ((color >> 16 & 0xFF) - 255) * amount);
        int g = Math.round(255 + ((color >> 8 & 0xFF) - 255) * amount);
        int b = Math.round(255 + ((color & 0xFF) - 255) * amount);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
