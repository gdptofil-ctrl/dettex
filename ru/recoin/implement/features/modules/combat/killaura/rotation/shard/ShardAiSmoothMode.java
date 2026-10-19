/*
 * New rotation "Shard AI". Uses the imported Space/RotationUtil code as-is.
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.shard;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public class ShardAiSmoothMode
extends AngleSmoothMode {
    private static final float SPEED = 0.7f;

    public ShardAiSmoothMode() {
        super("Shard AI");
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
        float[] targetRotations = entity != null ? RotationUtil.getRotations(entity) : new float[]{target.getYaw(), target.getPitch()};
        float[] smoothed = RotationUtil.smooth(currentRotations, targetRotations, SPEED);
        smoothed = RotationUtil.applyGCD(smoothed, currentRotations);
        float pitch = class_3532.method_15363(smoothed[1], -90.0f, 90.0f);
        return new Angle(smoothed[0], pitch);
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}
