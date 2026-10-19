/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_241
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation;

import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;

public final class AngleUtil
implements QuickImports {
    public static Angle fromVec2f(class_241 vector2f) {
        return new Angle(vector2f.field_1342, vector2f.field_1343);
    }

    public static Angle fromVec3d(class_243 vector) {
        return new Angle((float)class_3532.method_15338((double)(Math.toDegrees(Math.atan2(vector.field_1350, vector.field_1352)) - 90.0)), (float)class_3532.method_15338((double)Math.toDegrees(-Math.atan2(vector.field_1351, Math.hypot(vector.field_1352, vector.field_1350)))));
    }

    public static Angle calculateDelta(Angle start, Angle end) {
        float deltaYaw = class_3532.method_15393((float)(end.getYaw() - start.getYaw()));
        float deltaPitch = class_3532.method_15393((float)(end.getPitch() - start.getPitch()));
        return new Angle(deltaYaw, deltaPitch);
    }

    public static Angle calculateAngle(class_243 to) {
        return AngleUtil.fromVec3d(to.method_1020(AngleUtil.mc.field_1724.method_33571()));
    }

    public static Angle pitch(float pitch) {
        return new Angle(AngleUtil.mc.field_1724.method_36454(), pitch);
    }

    public static Angle cameraAngle() {
        return new Angle(AngleUtil.mc.field_1724.method_36454(), AngleUtil.mc.field_1724.method_36455());
    }

    private AngleUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

