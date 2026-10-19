/*
 * Ported from Space/RotationUtil: smooth (speed 0.7) + applyGCD.
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationUtil;

public class SlothSmoothMode
extends AngleSmoothMode {
    public SlothSmoothMode() {
        super("sloth");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 vec, class_1297 entity) {
        if (target == null) {
            return current;
        }
        if (current == null) {
            current = AngleUtil.cameraAngle();
        }
        float[] currentRotations = new float[]{current.getYaw(), current.getPitch()};
        float[] targetRotations = new float[]{target.getYaw(), target.getPitch()};
        float[] smoothed = RotationUtil.smooth(currentRotations, targetRotations, 0.7f);
        smoothed = RotationUtil.applyGCD(smoothed, currentRotations);
        float pitch = class_3532.method_15363(smoothed[1], -90.0f, 90.0f);
        return new Angle(smoothed[0], pitch);
    }

    @Override
    public class_243 randomValue() {
        return new class_243(0.0, 0.0, 0.0);
    }
}
