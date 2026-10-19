/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 */
package ru.recoin.common.util.crystal;

import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import ru.recoin.common.QuickImports;

public final class CombatRaycast
implements QuickImports {
    public static class_243 trace(float amount, float amount2) {
        float amount3 = -amount * ((float)Math.PI / 180);
        float amount4 = -amount2 * ((float)Math.PI / 180);
        float amount5 = (float)Math.cos(amount4);
        return new class_243(Math.sin(amount3) * (double)amount5, Math.sin(amount4), Math.cos(amount3) * (double)amount5);
    }

    private static double trace(class_243 vector, class_243 vector2, class_243 vector3) {
        class_243 vector4 = vector3.method_1020(vector2);
        double distance = vector4.method_1027();
        if (distance == 0.0) {
            return vector.method_1025(vector2);
        }
        double distance2 = Math.max(0.0, Math.min(1.0, vector.method_1020(vector2).method_1026(vector4) / distance));
        return vector.method_1025(vector2.method_1019(vector4.method_1021(distance2)));
    }

    public static boolean trace(class_238 box, float amount, float amount2, double distance, boolean flag) {
        if (CombatRaycast.mc.field_1724 != null && CombatRaycast.mc.field_1687 != null) {
            class_243 eyePosition = CombatRaycast.mc.field_1724.method_33571();
            if (box.method_1006(eyePosition)) {
                return true;
            }
            class_243 vector = eyePosition.method_1019(CombatRaycast.trace(amount, amount2).method_1021(distance));
            Optional value = box.method_992(eyePosition, vector);
            if (value.isEmpty()) {
                return false;
            }
            if (!flag) {
                return true;
            }
            class_3965 hit = CombatRaycast.mc.field_1687.method_17742(new class_3959(eyePosition, (class_243)value.get(), class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)CombatRaycast.mc.field_1724));
            return hit.method_17783() != class_239.class_240.field_1332 || eyePosition.method_1025(hit.method_17784()) >= eyePosition.method_1025((class_243)value.get());
        }
        return false;
    }

    public static boolean trace(class_243 vector, float amount, float amount2, double distance, double distance2) {
        class_243 vector2;
        class_243 eyePosition = CombatRaycast.mc.field_1724.method_33571();
        return CombatRaycast.trace(vector, eyePosition, vector2 = eyePosition.method_1019(CombatRaycast.trace(amount, amount2).method_1021(distance))) <= distance2 * distance2;
    }

    private CombatRaycast() {
    }

    public static boolean trace(class_1297 entity, float amount, float amount2, double distance, boolean flag) {
        return CombatRaycast.trace(entity.method_5829(), amount, amount2, distance, flag);
    }
}

