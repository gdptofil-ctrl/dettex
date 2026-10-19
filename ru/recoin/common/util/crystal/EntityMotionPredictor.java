/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1293
 *  net.minecraft.class_1294
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2680
 *  net.minecraft.class_3481
 *  net.minecraft.class_3486
 *  net.minecraft.class_3610
 */
package ru.recoin.common.util.crystal;

import java.util.List;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.minecraft.class_3610;

public final class EntityMotionPredictor {
    public static PredictedMotion predict(class_1309 entity, int count) {
        class_238 box = entity.method_5829();
        class_243 vector = entity instanceof class_1657 ? new class_243(entity.method_23317() - entity.field_6014, entity.method_23318() - entity.field_6036, entity.method_23321() - entity.field_5969) : entity.method_18798();
        boolean flag = entity.method_24828();
        for (int index = 0; index < count; ++index) {
            class_243 vector2 = class_1297.method_20736((class_1297)entity, (class_243)vector, (class_238)box, (class_1937)entity.method_73183(), List.of());
            box = box.method_997(vector2);
            boolean flag2 = vector.field_1352 != vector2.field_1352 || vector.field_1350 != vector2.field_1350;
            boolean flag3 = vector.field_1351 != vector2.field_1351;
            flag = flag3 && vector.field_1351 < 0.0;
            double distance = vector.field_1352 != vector2.field_1352 ? 0.0 : vector2.field_1352;
            double distance2 = vector.field_1350 != vector2.field_1350 ? 0.0 : vector2.field_1350;
            double distance3 = flag ? 0.0 : vector2.field_1351;
            boolean flag4 = EntityMotionPredictor.intersectsFluid(entity, box, true);
            boolean flag5 = EntityMotionPredictor.intersectsFluid(entity, box, false);
            if (entity.method_6128()) {
                vector = new class_243(distance * 0.99, distance3 * 0.98, distance2 * 0.99);
                continue;
            }
            if (flag4) {
                vector = new class_243(distance * 0.8, (distance3 - 0.08) * 0.8, distance2 * 0.8);
                if (!flag2 || !EntityMotionPredictor.hasCollisionFreeMovement(entity, box, vector.field_1352, 0.6, vector.field_1350)) continue;
                vector = new class_243(vector.field_1352, 0.3, vector.field_1350);
                continue;
            }
            if (flag5) {
                vector = new class_243(distance * 0.5, (distance3 - 0.02) * 0.5, distance2 * 0.5);
                if (!flag2 || !EntityMotionPredictor.hasCollisionFreeMovement(entity, box, vector.field_1352, 0.6, vector.field_1350)) continue;
                vector = new class_243(vector.field_1352, 0.3, vector.field_1350);
                continue;
            }
            class_2338 position = class_2338.method_49637((double)((box.field_1323 + box.field_1320) * 0.5), (double)box.field_1322, (double)((box.field_1321 + box.field_1324) * 0.5));
            if (entity.method_73183().method_8320(position).method_26164(class_3481.field_22414)) {
                distance = Math.max(-0.15, Math.min(0.15, distance));
                distance2 = Math.max(-0.15, Math.min(0.15, distance2));
                distance3 = Math.max(distance3, -0.15);
                if (flag2) {
                    distance3 = 0.2;
                }
            }
            double distance4 = entity.method_6059(class_1294.field_5906) && distance3 <= 0.0 ? 0.01 : 0.08;
            class_1293 effect = entity.method_6112(class_1294.field_5902);
            if (effect != null) {
                distance3 += (0.05 * (double)(effect.method_5578() + 1) - distance3) * 0.2;
            } else if (!entity.method_5740()) {
                distance3 -= distance4;
            }
            class_2338 position2 = class_2338.method_49637((double)((box.field_1323 + box.field_1320) * 0.5), (double)(box.field_1322 - 0.5000001), (double)((box.field_1321 + box.field_1324) * 0.5));
            class_2680 state = entity.method_73183().method_8320(position2);
            float amount = flag ? state.method_26204().method_9499() * 0.91f : 0.91f;
            vector = new class_243(distance * (double)amount, distance3 * 0.98, distance2 * (double)amount);
        }
        return new PredictedMotion(box, new class_243((box.field_1323 + box.field_1320) * 0.5, box.field_1322, (box.field_1321 + box.field_1324) * 0.5));
    }

    private EntityMotionPredictor() {
    }

    private static boolean hasCollisionFreeMovement(class_1309 entity, class_238 box, double distance, double distance2, double distance3) {
        return class_1297.method_20736((class_1297)entity, (class_243)new class_243(distance, distance2, distance3), (class_238)box, (class_1937)entity.method_73183(), List.of()).equals((Object)new class_243(distance, distance2, distance3));
    }

    private static boolean intersectsFluid(class_1309 entity, class_238 box, boolean flag) {
        int count = (int)Math.floor(box.field_1323);
        int count2 = (int)Math.floor(box.field_1322);
        int count3 = (int)Math.floor(box.field_1321);
        int count4 = (int)Math.floor(box.field_1320 - 1.0E-7);
        int count5 = (int)Math.floor(box.field_1325 - 1.0E-7);
        int count6 = (int)Math.floor(box.field_1324 - 1.0E-7);
        for (int index = count; index <= count4; ++index) {
            for (int index2 = count2; index2 <= count5; ++index2) {
                for (int index3 = count3; index3 <= count6; ++index3) {
                    class_3610 value = entity.method_73183().method_8316(new class_2338(index, index2, index3));
                    if (!(flag ? value.method_15767(class_3486.field_15517) : value.method_15767(class_3486.field_15518))) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public record PredictedMotion(class_238 box, class_243 position) {
    }
}

