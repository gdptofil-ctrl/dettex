/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_241
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.common.util.crystal;

import java.util.Objects;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.crystal.AimAngles;

public final class AimQuantizer
implements QuickImports {
    public static class_243 closestPointToEntity(class_243 vector, class_1297 entity) {
        return AimQuantizer.closestPoint(vector, entity.method_5829());
    }

    private AimQuantizer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static float mouseSteps(float amount) {
        return Math.round(amount / AimQuantizer.getStep());
    }

    public static boolean intersectsBox(class_243 vector, double distance, class_238 box) {
        class_243 eyePosition = Objects.requireNonNull(AimQuantizer.mc.field_1724).method_33571();
        return box.method_1006(eyePosition) || box.method_992(eyePosition, eyePosition.method_1019(vector.method_1021(distance))).isPresent();
    }

    public static class_243 nearestTargetVector(class_1297 entity) {
        class_243 eyePosition = AimQuantizer.mc.field_1724.method_5836(mc.method_61966().method_60637(true));
        return AimQuantizer.closestPointToEntity(eyePosition, entity).method_1020(eyePosition);
    }

    public static class_243 closestPoint(class_243 vector, class_238 box) {
        return new class_243(Math.clamp(vector.method_10216(), box.field_1323, box.field_1320), Math.clamp(vector.method_10214(), box.field_1322, box.field_1325), Math.clamp(vector.method_10215(), box.field_1321, box.field_1324));
    }

    public static float quantize(float amount) {
        return (float)Math.round(amount / AimQuantizer.getStep()) * AimQuantizer.getStep();
    }

    public static class_241 angleToCoordinates(double distance, double distance2, double distance3) {
        double distance4 = distance - AimQuantizer.mc.field_1724.method_23317();
        double distance5 = distance2 - AimQuantizer.mc.field_1724.method_23320();
        double distance6 = distance3 - AimQuantizer.mc.field_1724.method_23321();
        double distance7 = class_3532.method_15355((float)((float)(distance4 * distance4 + distance6 * distance6)));
        float amount = (float)(class_3532.method_15349((double)distance6, (double)distance4) * 180.0 / Math.PI - 90.0);
        float amount2 = (float)(-class_3532.method_15349((double)distance5, (double)distance7) * 180.0 / Math.PI);
        return new class_241(amount, amount2);
    }

    public static float truncateToStep(float amount) {
        float amount2 = AimQuantizer.getStep();
        return amount2 <= 0.0f ? amount : amount - amount % amount2;
    }

    public static float getSensitivityScale() {
        double distance = (Double)AimQuantizer.mc.field_1690.method_42495().method_41753();
        return (float)(Math.pow(distance * (double)0.6f + (double)0.2f, 3.0) * 8.0);
    }

    public static float roundToStep(float amount) {
        return AimQuantizer.mouseSteps(amount) * AimQuantizer.getStep();
    }

    public static boolean intersectsAngles(AimAngles angle, double distance, class_238 box) {
        class_243 eyePosition = Objects.requireNonNull(AimQuantizer.mc.field_1724).method_33571();
        return box.method_1006(eyePosition) || box.method_992(eyePosition, eyePosition.method_1019(angle.toVector().method_1021(distance))).isPresent();
    }

    public static class_241 angleToPosition(class_243 vector) {
        return AimQuantizer.angleToCoordinates(vector.field_1352, vector.field_1351, vector.field_1350);
    }

    public static float getStep() {
        double distance = 0.15;
        return (float)((double)AimQuantizer.getSensitivityScale() * distance);
    }
}

