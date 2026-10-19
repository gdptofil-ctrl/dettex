/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public class LinearSmoothMode
extends AngleSmoothMode {
    public LinearSmoothMode() {
        super("Linear");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, class_243 vec3d, class_1297 entity) {
        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float)Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        float straightLineYaw = Math.abs(yawDelta / rotationDifference) * 360.0f;
        float straightLinePitch = Math.abs(pitchDelta / rotationDifference) * 360.0f;
        return new Angle(currentAngle.getYaw() + Math.min(Math.max(yawDelta, -straightLineYaw), straightLineYaw), currentAngle.getPitch() + Math.min(Math.max(pitchDelta, -straightLinePitch), straightLinePitch));
    }

    @Override
    public class_243 randomValue() {
        return new class_243(0.0, 0.0, 0.0);
    }
}

