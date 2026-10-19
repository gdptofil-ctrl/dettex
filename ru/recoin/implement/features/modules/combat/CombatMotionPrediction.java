/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.QuickImports;

final class CombatMotionPrediction
implements QuickImports {
    private CombatMotionPrediction() {
    }

    static class_243[] path(class_1309 entity, int ticks) {
        class_243[] positions = new class_243[ticks + 1];
        positions[0] = entity.method_73189();
        class_243 velocity = entity.method_18798();
        class_243 observed = positions[0].method_1020(entity.method_30950(0.0f));
        if (entity != CombatMotionPrediction.mc.field_1724 && observed.method_1027() > 1.0E-4 && observed.method_1027() < 4.0) {
            class_243 class_2432 = velocity = entity.field_6235 > 0 && velocity.method_1027() > observed.method_1027() * 1.5 ? velocity : observed;
        }
        if (velocity.method_1027() > 4.0) {
            velocity = velocity.method_1029().method_1021(2.0);
        }
        class_238 box = entity.method_5829();
        for (int i = 1; i <= ticks; ++i) {
            double dy = CombatMotionPrediction.clip((class_1309)entity, (class_238)box, (class_243)new class_243((double)0.0, (double)velocity.field_1351, (double)0.0)).field_1351;
            box = box.method_989(0.0, dy, 0.0);
            double dx = CombatMotionPrediction.clip((class_1309)entity, (class_238)box, (class_243)new class_243((double)velocity.field_1352, (double)0.0, (double)0.0)).field_1352;
            box = box.method_989(dx, 0.0, 0.0);
            double dz = CombatMotionPrediction.clip((class_1309)entity, (class_238)box, (class_243)new class_243((double)0.0, (double)0.0, (double)velocity.field_1350)).field_1350;
            box = box.method_989(0.0, 0.0, dz);
            positions[i] = positions[i - 1].method_1031(dx, dy, dz);
            boolean grounded = !CombatMotionPrediction.mc.field_1687.method_8587((class_1297)entity, box.method_989(0.0, -0.04, 0.0));
            boolean floating = entity.method_6128() || entity.method_5740();
            double drag = entity.method_5799() ? 0.8 : (grounded && entity.field_6235 == 0 || floating ? 1.0 : (grounded ? 0.6 : 0.91));
            double gravity = floating ? 0.0 : (entity.method_5799() ? 0.02 : 0.08);
            velocity = new class_243(dx * drag, grounded && dy <= 0.0 ? 0.0 : (dy - gravity) * (floating ? 1.0 : 0.98), dz * drag);
        }
        return positions;
    }

    static class_243 at(class_243[] path, double tick) {
        double time = class_3532.method_15350((double)tick, (double)0.0, (double)(path.length - 1));
        int index = (int)time;
        return path[index].method_35590(path[Math.min(index + 1, path.length - 1)], time - (double)index);
    }

    private static class_243 clip(class_1309 entity, class_238 box, class_243 movement) {
        if (movement.method_1027() < 1.0E-10 || CombatMotionPrediction.mc.field_1687.method_8587((class_1297)entity, box.method_18804(movement))) {
            return movement;
        }
        double low = 0.0;
        double high = 1.0;
        for (int i = 0; i < 8; ++i) {
            double middle = (low + high) * 0.5;
            if (CombatMotionPrediction.mc.field_1687.method_8587((class_1297)entity, box.method_18804(movement.method_1021(middle)))) {
                low = middle;
                continue;
            }
            high = middle;
        }
        return movement.method_1021(low);
    }
}

