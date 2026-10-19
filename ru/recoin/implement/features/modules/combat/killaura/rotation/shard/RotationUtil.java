/*
 * Imported from Space/RotationUtil. Logic unchanged, only imports adapted.
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.shard;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.math.MathUtil;

public class RotationUtil
implements QuickImports {
    public static float[] getRotations(class_1297 target) {
        if (mc.field_1724 == null || target == null) {
            return new float[]{0.0f, 0.0f};
        }
        class_243 eyes = mc.field_1724.method_33571();
        class_243 targetPos = target.method_33571();
        double diffX = targetPos.field_1352 - eyes.field_1352;
        double diffY = targetPos.field_1351 - eyes.field_1351;
        double diffZ = targetPos.field_1350 - eyes.field_1350;
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, dist)));
        return new float[]{
            class_3532.method_15393(yaw),
            class_3532.method_15363(class_3532.method_15393(pitch), -90.0f, 90.0f)
        };
    }

    public static float[] smooth(float[] current, float[] target, float speed) {
        float yaw = current[0] + class_3532.method_15393(target[0] - current[0]) * speed;
        float pitch = current[1] + (target[1] - current[1]) * speed;
        return new float[]{yaw, pitch};
    }

    public static float[] applyGCD(float[] current, float[] previous) {
        double gcd = MathUtil.computeGcd();
        float deltaYaw = current[0] - previous[0];
        float deltaPitch = current[1] - previous[1];
        deltaYaw = (float)((double)deltaYaw - (double)deltaYaw % gcd);
        deltaPitch = (float)((double)deltaPitch - (double)deltaPitch % gcd);
        return new float[]{previous[0] + deltaYaw, previous[1] + deltaPitch};
    }
}
