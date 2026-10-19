/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1267
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1887
 *  net.minecraft.class_1890
 *  net.minecraft.class_1893
 *  net.minecraft.class_1922
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_3532
 *  net.minecraft.class_3726
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_5134
 *  net.minecraft.class_5321
 *  net.minecraft.class_6880
 *  net.minecraft.class_7924
 */
package ru.recoin.common.util.crystal;

import java.util.Set;
import net.minecraft.class_1267;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1893;
import net.minecraft.class_1922;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_3532;
import net.minecraft.class_3726;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_5134;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import net.minecraft.class_7924;

public final class ExplosionDamageCalculator {
    private static double calculateExposure(class_243 vector, class_1309 entity, class_238 box, class_2338 position, Set<class_2338> positions) {
        double distance = 1.0 / ((box.field_1320 - box.field_1323) * 2.0 + 1.0);
        double distance2 = 1.0 / ((box.field_1325 - box.field_1322) * 2.0 + 1.0);
        double distance3 = 1.0 / ((box.field_1324 - box.field_1321) * 2.0 + 1.0);
        double distance4 = (1.0 - Math.floor(1.0 / distance) * distance) / 2.0;
        double distance5 = (1.0 - Math.floor(1.0 / distance3) * distance3) / 2.0;
        int count = 0;
        int count2 = 0;
        for (double distance6 = 0.0; distance6 <= 1.0; distance6 += distance) {
            for (double distance7 = 0.0; distance7 <= 1.0; distance7 += distance2) {
                for (double distance8 = 0.0; distance8 <= 1.0; distance8 += distance3) {
                    class_243 vector2 = new class_243(class_3532.method_16436((double)distance6, (double)box.field_1323, (double)box.field_1320) + distance4, class_3532.method_16436((double)distance7, (double)box.field_1322, (double)box.field_1325), class_3532.method_16436((double)distance8, (double)box.field_1321, (double)box.field_1324) + distance5);
                    if (ExplosionDamageCalculator.hasClearRay(vector2, vector, entity, position, positions)) {
                        ++count;
                    }
                    ++count2;
                }
            }
        }
        return count2 == 0 ? 0.0 : (double)count / (double)count2;
    }

    private static boolean hasClearRay(class_243 vector, class_243 vector2, class_1309 entity, class_2338 position, Set<class_2338> positions) {
        if (position == null && positions.isEmpty()) {
            class_3965 hit = entity.method_73183().method_17742(new class_3959(vector, vector2, class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)entity));
            return hit.method_17783() == class_239.class_240.field_1333;
        }
        return (Boolean)class_1922.method_17744((class_243)vector, (class_243)vector2, entity.method_73183(), (target, value) -> {
            boolean flag = value.equals((Object)position);
            if (!flag && positions.contains(value)) {
                return null;
            }
            class_2680 state = flag ? class_2246.field_10540.method_9564() : target.method_8320(value);
            class_265 value2 = state.method_26194((class_1922)target, value, class_3726.method_16195((class_1297)entity));
            return target.method_17745(vector, vector2, value, value2, state) == null ? null : Boolean.FALSE;
        }, context -> Boolean.TRUE);
    }

    public static float calculate(class_243 vector, class_1309 entity, class_238 box, class_2338 position, class_2338 position2) {
        return ExplosionDamageCalculator.calculate(vector, entity, box, position, position2 == null ? Set.of() : Set.of(position2));
    }

    private ExplosionDamageCalculator() {
    }

    public static float calculate(class_243 vector, class_1309 entity, class_238 box, class_2338 position, Set<class_2338> positions) {
        Set<class_2338> positions2 = positions == null ? Set.of() : positions;
        class_243 vector2 = new class_243((box.field_1323 + box.field_1320) * 0.5, box.field_1322, (box.field_1321 + box.field_1324) * 0.5);
        double distance = vector2.method_1022(vector) / 12.0;
        if (distance > 1.0) {
            return 0.0f;
        }
        double distance2 = (1.0 - distance) * ExplosionDamageCalculator.calculateExposure(vector, entity, box, position, positions2);
        float amount = (float)((distance2 * distance2 + distance2) * 42.0 + 1.0);
        if (entity instanceof class_1657) {
            class_1267 value = entity.method_73183().method_8407();
            amount = switch (value) {
                case class_1267.field_5801 -> 0.0f;
                case class_1267.field_5805 -> Math.min(amount * 0.5f + 1.0f, amount);
                case class_1267.field_5807 -> amount * 1.5f;
                case class_1267.field_5802 -> amount;
                default -> throw new MatchException(null, null);
            };
        }
        float amount2 = entity.method_6096();
        float amount3 = (float)entity.method_45325(class_5134.field_23725);
        float amount4 = class_3532.method_15363((float)(amount2 - amount / (2.0f + amount3 / 4.0f)), (float)(amount2 * 0.2f), (float)20.0f);
        float amount5 = amount * (1.0f - amount4 / 25.0f);
        int count = 0;
        for (class_1304 value2 : new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166}) {
            class_1799 stack = entity.method_6118(value2);
            count += ExplosionDamageCalculator.enchantmentLevel(entity, stack, (class_5321<class_1887>)class_1893.field_9111);
            count += ExplosionDamageCalculator.enchantmentLevel(entity, stack, (class_5321<class_1887>)class_1893.field_9107) * 2;
        }
        count = class_3532.method_15340((int)count, (int)0, (int)20);
        amount5 *= 1.0f - (float)count / 25.0f;
        class_1293 effect = entity.method_6112(class_1294.field_5907);
        if (effect != null) {
            float amount6 = class_3532.method_15363((float)((float)(effect.method_5578() + 1) * 0.2f), (float)0.0f, (float)1.0f);
            amount5 *= 1.0f - amount6;
        }
        return Math.max(0.0f, amount5);
    }

    public static float calculate(class_243 vector, class_1309 entity, class_238 box, class_2338 position) {
        return ExplosionDamageCalculator.calculate(vector, entity, box, null, position == null ? Set.of() : Set.of(position));
    }

    public static float calculate(class_243 vector, class_1309 entity, class_238 box) {
        return ExplosionDamageCalculator.calculate(vector, entity, box, null, Set.of());
    }

    private static int enchantmentLevel(class_1309 entity, class_1799 stack, class_5321<class_1887> value) {
        if (stack.method_7960()) {
            return 0;
        }
        try {
            return entity.method_56673().method_30530(class_7924.field_41265).method_46746(value).map(target -> class_1890.method_8225((class_6880)target, (class_1799)stack)).orElse(0);
        }
        catch (RuntimeException exception) {
            return 0;
        }
    }

    public static float calculate(class_243 vector, class_1309 entity) {
        return ExplosionDamageCalculator.calculate(vector, entity, entity.method_5829(), null, Set.of());
    }
}

