/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1267
 *  net.minecraft.class_1280
 *  net.minecraft.class_1282
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1799
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_1922
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2378
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_5134
 *  net.minecraft.class_6880
 *  net.minecraft.class_7924
 */
package ru.recoin.implement.features.modules.combat;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1267;
import net.minecraft.class_1280;
import net.minecraft.class_1282;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2378;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3959;
import net.minecraft.class_5134;
import net.minecraft.class_6880;
import net.minecraft.class_7924;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.entity.EntityCompat;
import ru.recoin.implement.features.modules.combat.CombatMotionPrediction;

final class AutoCrystalDamage
implements QuickImports {
    private final Map<Integer, class_243[]> predictions = new HashMap<Integer, class_243[]>();
    private final Map<DamageKey, Float> damages = new HashMap<DamageKey, Float>();
    private final Map<Integer, Integer> protection = new HashMap<Integer, Integer>();

    AutoCrystalDamage() {
    }

    void clear() {
        this.predictions.clear();
        this.damages.clear();
        this.protection.clear();
    }

    class_243 predict(class_1309 entity, int ticks) {
        return CombatMotionPrediction.at(this.predictions.computeIfAbsent(entity.method_5628(), key -> CombatMotionPrediction.path(entity, 32)), ticks);
    }

    float estimate(class_243 explosion, class_1309 entity, float power, class_2338 obsidian, class_2338 removed, int ticks, boolean defensive) {
        if (ticks == 0) {
            return this.damage(explosion, entity, power, obsidian, removed, 0);
        }
        float predicted = this.damage(explosion, entity, power, obsidian, removed, ticks);
        if (!defensive) {
            float early = this.damage(explosion, entity, power, obsidian, removed, Math.max(0, ticks - 1));
            float late = this.damage(explosion, entity, power, obsidian, removed, ticks + 1);
            return Math.min(predicted, Math.min(early, late)) * 0.7f + predicted * 0.3f;
        }
        float current = this.damage(explosion, entity, power, obsidian, removed, 0);
        float halfway = ticks > 1 ? this.damage(explosion, entity, power, obsidian, removed, ticks / 2) : current;
        return Math.max(halfway, Math.max(current, predicted));
    }

    private float damage(class_243 explosion, class_1309 entity, float power, class_2338 obsidian, class_2338 removed, int ticks) {
        class_238 box;
        double exposure;
        DamageKey key = new DamageKey(entity.method_5628(), explosion, power, obsidian, removed, ticks);
        Float cached = this.damages.get(key);
        if (cached != null) {
            return cached.floatValue();
        }
        class_243 position = this.predict(entity, ticks);
        double radius = (double)power * 2.0;
        double distanceSquared = position.method_1025(explosion);
        if (distanceSquared >= radius * radius) {
            this.damages.put(key, Float.valueOf(0.0f));
            return 0.0f;
        }
        double distance = Math.sqrt(distanceSquared) / radius;
        double impact = (1.0 - distance) * (exposure = this.exposure(explosion, box = entity.method_5829().method_997(position.method_1020(entity.method_73189())), entity, obsidian, removed));
        float damage = impact <= 0.0 ? 0.0f : (float)((int)((impact * impact + impact) * 7.0 * (double)power + 1.0));
        damage = switch (AutoCrystalDamage.mc.field_1687.method_8407()) {
            default -> throw new MatchException(null, null);
            case class_1267.field_5801 -> 0.0f;
            case class_1267.field_5805 -> Math.min(damage * 0.5f + 1.0f, damage);
            case class_1267.field_5802 -> damage;
            case class_1267.field_5807 -> damage * 1.5f;
        };
        class_1282 source = power == 5.0f ? AutoCrystalDamage.mc.field_1687.method_48963().method_48808(explosion) : AutoCrystalDamage.mc.field_1687.method_48963().method_48819(null, null);
        damage = class_1280.method_5496((class_1309)entity, (float)damage, (class_1282)source, (float)entity.method_6096(), (float)((float)entity.method_45325(class_5134.field_23725)));
        class_1293 resistance = entity.method_6112(class_1294.field_5907);
        if (resistance != null) {
            damage *= Math.max(0.0f, 1.0f - 0.2f * (float)(resistance.method_5578() + 1));
        }
        this.damages.put(key, Float.valueOf(damage *= 1.0f - (float)Math.min(20, this.protection.computeIfAbsent(entity.method_5628(), id -> this.protection(entity))) / 25.0f));
        return damage;
    }

    private double exposure(class_243 explosion, class_238 box, class_1309 entity, class_2338 obsidian, class_2338 removed) {
        double stepX = 1.0 / (box.method_17939() * 2.0 + 1.0);
        double stepY = 1.0 / (box.method_17940() * 2.0 + 1.0);
        double stepZ = 1.0 / (box.method_17941() * 2.0 + 1.0);
        double offsetX = (1.0 - Math.floor(1.0 / stepX) * stepX) * 0.5;
        double offsetZ = (1.0 - Math.floor(1.0 / stepZ) * stepZ) * 0.5;
        int clear = 0;
        int total = 0;
        for (double x = 0.0; x <= 1.0; x += stepX) {
            for (double y = 0.0; y <= 1.0; y += stepY) {
                for (double z = 0.0; z <= 1.0; z += stepZ) {
                    class_3959 context;
                    class_243 sample = new class_243(box.field_1323 + box.method_17939() * x + offsetX, box.field_1322 + box.method_17940() * y, box.field_1321 + box.method_17941() * z + offsetZ);
                    boolean blocked = (Boolean)class_1922.method_17744((class_243)sample, (class_243)explosion, (context = new class_3959(sample, explosion, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)entity)), (ray, pos) -> {
                        if (pos.equals((Object)removed)) {
                            return null;
                        }
                        class_2680 state = pos.equals((Object)obsidian) ? class_2246.field_10540.method_9564() : AutoCrystalDamage.mc.field_1687.method_8320(pos);
                        return ray.method_17748(state, (class_1922)AutoCrystalDamage.mc.field_1687, pos).method_1092(sample, explosion, pos) != null ? Boolean.TRUE : null;
                    }, ray -> Boolean.FALSE);
                    if (!blocked) {
                        ++clear;
                    }
                    ++total;
                }
            }
        }
        return total == 0 ? 0.0 : (double)clear / (double)total;
    }

    private int protection(class_1309 entity) {
        class_6880 general = AutoCrystalDamage.mc.field_1687.method_30349().method_30530(class_7924.field_41265).method_10223(class_1893.field_9111.method_29177()).orElse(null);
        class_6880 blast = AutoCrystalDamage.mc.field_1687.method_30349().method_30530(class_7924.field_41265).method_10223(class_1893.field_9107.method_29177()).orElse(null);
        int value = 0;
        for (class_1799 stack : EntityCompat.getArmorItems(entity)) {
            if (general != null) {
                value += class_1890.method_8225((class_6880)general, (class_1799)stack);
            }
            if (blast == null) continue;
            value += class_1890.method_8225((class_6880)blast, (class_1799)stack) * 2;
        }
        return value;
    }

    private record DamageKey(int entity, class_243 explosion, float power, class_2338 obsidian, class_2338 removed, int ticks) {
    }
}

