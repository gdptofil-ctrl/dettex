/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public final class WindChargeSmoothMode
extends AngleSmoothMode {
    public WindChargeSmoothMode() {
        super("WindCharge");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 point, class_1297 entity) {
        if (target == null) {
            return current;
        }
        Angle delta = AngleUtil.calculateDelta(current, target);
        float factor = target.getPitch() >= 85.0f ? 1.0f : 0.35f;
        float yaw = current.getYaw() + delta.getYaw() * factor;
        float pitch = current.getPitch() + (target.getPitch() - current.getPitch()) * factor;
        return new Angle(yaw, class_3532.method_15363((float)pitch, (float)-90.0f, (float)90.0f));
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}

