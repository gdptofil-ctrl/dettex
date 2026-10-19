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

public class SmoothMode
extends AngleSmoothMode {
    private static final float YAW_SPEED = 42.0f;
    private static final float PITCH_SPEED = 22.0f;

    public SmoothMode() {
        super("Smooth");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, class_243 vec3d, class_1297 entity) {
        if (targetAngle == null) {
            return currentAngle;
        }
        Angle delta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yaw = currentAngle.getYaw() + class_3532.method_15363((float)delta.getYaw(), (float)-42.0f, (float)42.0f);
        float pitch = currentAngle.getPitch() + class_3532.method_15363((float)delta.getPitch(), (float)-22.0f, (float)22.0f);
        return new Angle(yaw, class_3532.method_15363((float)pitch, (float)-90.0f, (float)90.0f));
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}

