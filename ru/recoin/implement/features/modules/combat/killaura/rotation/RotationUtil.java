/*
 * Ported from Space/RotationUtil.
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation;

import net.minecraft.class_3532;
import ru.recoin.common.util.math.MathUtil;

public final class RotationUtil {
    public static float[] smooth(float[] current, float[] target, float speed) {
        float yaw = current[0] + class_3532.method_15393((float)(target[0] - current[0])) * speed;
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

    private RotationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
