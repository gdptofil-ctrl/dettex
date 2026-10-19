/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_241
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.class_241;
import net.minecraft.class_243;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;

final class RotationQuantizationSupport
implements QuickImports {
    private static final double FIXED_SENSITIVITY_BASE = 0.502112676056338;
    private static final double SENSITIVITY_SCALE = (double)0.6f;
    private static final double SENSITIVITY_OFFSET = (double)0.2f;
    private static final double ROTATION_SCALE = (double)0.15f;

    private RotationQuantizationSupport() {
    }

    static class_241 toVec2(Angle angle) {
        return angle == null ? null : new class_241(angle.getYaw(), angle.getPitch());
    }

    static Angle toAngle(class_241 rotation) {
        return rotation == null ? null : new Angle(rotation.field_1343, rotation.field_1342);
    }

    static class_241 targetRotation(class_243 point) {
        class_243 direction = point.method_1020(RotationQuantizationSupport.mc.field_1724.method_33571()).method_1029();
        float yaw = (float)Math.toDegrees(Math.atan2(-direction.field_1352, direction.field_1350));
        float pitch = (float)Math.toDegrees(Math.asin(direction.field_1351));
        return new class_241(yaw, -pitch);
    }

    static float quantizeFixed(float rotation) {
        double step = 0.15190945388658073;
        return step <= 0.0 ? rotation : (float)((double)Math.round((double)rotation / step) * step);
    }

    static float quantizeSensitivity(float rotation) {
        double sensitivity = (Double)RotationQuantizationSupport.mc.field_1690.method_42495().method_41753();
        if (!Double.isFinite(sensitivity) || sensitivity < 0.0 || sensitivity > 1.0) {
            throw new IllegalArgumentException("Sensitivity must be within 0..1");
        }
        boolean scoped = RotationQuantizationSupport.mc.field_1724 != null && !RotationQuantizationSupport.mc.field_1690.field_1914 && RotationQuantizationSupport.mc.field_1690.method_31044().method_31034() && RotationQuantizationSupport.mc.field_1724.method_31550();
        double base = sensitivity * (double)0.6f + (double)0.2f;
        double gain = base * base * base * (scoped ? 1.0 : 8.0);
        if (!Float.isFinite(rotation) || !Double.isFinite(gain) || gain <= 0.0) {
            throw new IllegalArgumentException("Invalid mouse delta or gain");
        }
        long counts = Math.round((double)rotation / (gain * (double)0.15f));
        return (float)((double)counts * gain) * 0.15f;
    }
}

