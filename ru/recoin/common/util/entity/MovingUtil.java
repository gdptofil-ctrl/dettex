/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10185
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.common.util.entity;

import java.util.Objects;
import net.minecraft.class_10185;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.QuickImports;

public final class MovingUtil
implements QuickImports {
    public static boolean hasPlayerMovement() {
        return MovingUtil.mc.field_1724.field_3913.method_3128().field_1342 != 0.0f || MovingUtil.mc.field_1724.field_3913.method_3128().field_1343 != 0.0f;
    }

    public static double[] calculateDirection(double distance) {
        return MovingUtil.calculateDirection(MovingUtil.mc.field_1724.field_3913.method_3128().field_1342, MovingUtil.mc.field_1724.field_3913.method_3128().field_1343, distance);
    }

    public static double[] calculateDirection(float forward, float sideways, double distance) {
        float yaw = MovingUtil.mc.field_1724.method_36454();
        if (forward != 0.0f) {
            if (sideways > 0.0f) {
                yaw += forward > 0.0f ? -45.0f : 45.0f;
            } else if (sideways < 0.0f) {
                yaw += forward > 0.0f ? 45.0f : -45.0f;
            }
            sideways = 0.0f;
            forward = forward > 0.0f ? 1.0f : -1.0f;
        }
        double sinYaw = Math.sin(Math.toRadians(yaw + 90.0f));
        double cosYaw = Math.cos(Math.toRadians(yaw + 90.0f));
        double xMovement = (double)forward * distance * cosYaw + (double)sideways * distance * sinYaw;
        double zMovement = (double)forward * distance * sinYaw - (double)sideways * distance * cosYaw;
        return new double[]{xMovement, zMovement};
    }

    public static double getSpeedSqrt(class_1297 entity) {
        if (entity == null) {
            return 0.0;
        }
        double dx = entity.method_23317() - entity.field_6014;
        double dy = entity.method_23318() - entity.field_6036;
        double dz = entity.method_23321() - entity.field_5969;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static void setVelocity(double velocity) {
        double[] direction = MovingUtil.calculateDirection(velocity);
        Objects.requireNonNull(MovingUtil.mc.field_1724).method_18800(direction[0], MovingUtil.mc.field_1724.method_18798().method_10214(), direction[1]);
    }

    public static void setVelocity(double velocity, double y) {
        double[] direction = MovingUtil.calculateDirection(velocity);
        Objects.requireNonNull(MovingUtil.mc.field_1724).method_18800(direction[0], y, direction[1]);
    }

    public static double getDegreesRelativeToView(class_243 positionRelativeToPlayer, float yaw) {
        float optimalYaw = (float)Math.atan2(-positionRelativeToPlayer.field_1352, positionRelativeToPlayer.field_1350);
        double currentYaw = Math.toRadians(class_3532.method_15393((float)yaw));
        return Math.toDegrees(class_3532.method_15338((double)((double)optimalYaw - currentYaw)));
    }

    public static class_10185 getDirectionalInputForDegrees(class_10185 input, double dgs, float deadAngle) {
        boolean forwards = input.comp_3159();
        boolean backwards = input.comp_3160();
        boolean left = input.comp_3161();
        boolean right = input.comp_3162();
        if (dgs >= (double)(-90.0f + deadAngle) && dgs <= (double)(90.0f - deadAngle)) {
            forwards = true;
        } else if (dgs < (double)(-90.0f - deadAngle) || dgs > (double)(90.0f + deadAngle)) {
            backwards = true;
        }
        if (dgs >= (double)(0.0f + deadAngle) && dgs <= (double)(180.0f - deadAngle)) {
            right = true;
        } else if (dgs >= (double)(-180.0f + deadAngle) && dgs <= (double)(0.0f - deadAngle)) {
            left = true;
        }
        return new class_10185(forwards, backwards, left, right, input.comp_3163(), input.comp_3164(), input.comp_3165());
    }

    public static class_10185 getDirectionalInputForDegrees(class_10185 input, double dgs) {
        return MovingUtil.getDirectionalInputForDegrees(input, dgs, 20.0f);
    }

    private MovingUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

