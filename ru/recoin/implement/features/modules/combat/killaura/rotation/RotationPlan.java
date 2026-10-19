/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public class RotationPlan
implements QuickImports {
    private final Angle angle;
    private final class_243 vec3d;
    private final class_1297 entity;
    private final AngleSmoothMode angleSmooth;
    private final int ticksUntilReset;
    private final float resetThreshold;
    private final boolean moveCorrection;
    private final boolean freeCorrection;
    private final boolean targetCorrection;

    public Angle nextRotation(Angle fromAngle, boolean isResetting) {
        Angle nextAngle;
        if (isResetting) {
            Angle targetAngle = AngleUtil.fromVec2f(RotationPlan.mc.field_1724.method_5802());
            nextAngle = this.angleSmooth.limitAngleChange(fromAngle, targetAngle);
        } else {
            Angle targetAngle = this.angle;
            nextAngle = this.angleSmooth.limitAngleChange(fromAngle, targetAngle, this.vec3d, this.entity);
        }
        return nextAngle;
    }

    public Angle getAngle() {
        return this.angle;
    }

    public class_243 getVec3d() {
        return this.vec3d;
    }

    public class_1297 getEntity() {
        return this.entity;
    }

    public AngleSmoothMode getAngleSmooth() {
        return this.angleSmooth;
    }

    public int getTicksUntilReset() {
        return this.ticksUntilReset;
    }

    public float getResetThreshold() {
        return this.resetThreshold;
    }

    public boolean isMoveCorrection() {
        return this.moveCorrection;
    }

    public boolean isFreeCorrection() {
        return this.freeCorrection;
    }

    public boolean isTargetCorrection() {
        return this.targetCorrection;
    }

    public RotationPlan(Angle angle, class_243 vec3d, class_1297 entity, AngleSmoothMode angleSmooth, int ticksUntilReset, float resetThreshold, boolean moveCorrection, boolean freeCorrection, boolean targetCorrection) {
        this.angle = angle;
        this.vec3d = vec3d;
        this.entity = entity;
        this.angleSmooth = angleSmooth;
        this.ticksUntilReset = ticksUntilReset;
        this.resetThreshold = resetThreshold;
        this.moveCorrection = moveCorrection;
        this.freeCorrection = freeCorrection;
        this.targetCorrection = targetCorrection;
    }
}

