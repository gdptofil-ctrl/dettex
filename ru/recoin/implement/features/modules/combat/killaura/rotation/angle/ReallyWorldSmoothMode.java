/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.util.crystal.AimQuantizer;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.target.BacktrackPositionResolver;

public class ReallyWorldSmoothMode
extends AngleSmoothMode {
    public ReallyWorldSmoothMode() {
        super("ReallyWorld");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 vector, class_1297 entity) {
        if (ReallyWorldSmoothMode.mc.field_1724 == null) {
            return current;
        }
        float yawLimit = 360.0f;
        if (entity != null) {
            class_238 box = BacktrackPositionResolver.boundingBox(entity);
            target = AngleUtil.calculateAngle(box.method_1005());
            class_243 eye = ReallyWorldSmoothMode.mc.field_1724.method_5836(mc.method_61966().method_60637(false));
            class_243 end = eye.method_1019(ReallyWorldSmoothMode.mc.field_1724.method_5631(current.getPitch(), current.getYaw()).method_1021((double)Aura.getInstance().getAttackDistance()));
            if (box.method_1006(eye) || box.method_992(eye, end).isPresent()) {
                yawLimit = 0.0f;
            }
        }
        float gcd = AimQuantizer.getStep();
        float deltaYaw = class_3532.method_15393((float)(target.getYaw() - current.getYaw()));
        float deltaPitch = target.getPitch() - current.getPitch();
        float yawStep = AimQuantizer.roundToStep(class_3532.method_15363((float)deltaYaw, (float)(-yawLimit), (float)yawLimit));
        float pitchStep = AimQuantizer.roundToStep(class_3532.method_15363((float)deltaPitch, (float)-360.0f, (float)360.0f));
        float pitch = class_3532.method_15363((float)(current.getPitch() + pitchStep), (float)-90.0f, (float)90.0f);
        return new Angle(current.getYaw() + yawStep - yawStep % gcd, pitch - pitch % gcd);
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}

