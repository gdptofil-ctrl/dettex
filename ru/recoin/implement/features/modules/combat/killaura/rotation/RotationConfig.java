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
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationPlan;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.LinearSmoothMode;

public class RotationConfig {
    public static RotationConfig DEFAULT = new RotationConfig(new LinearSmoothMode(), true, true);
    private final boolean moveCorrection;
    private final boolean freeCorrection;
    private final boolean targetCorrection;
    private final AngleSmoothMode angleSmooth;
    private final int resetThreshold = 3;

    public RotationConfig(boolean moveCorrection, boolean freeCorrection) {
        this(new LinearSmoothMode(), moveCorrection, freeCorrection);
    }

    public RotationConfig(boolean moveCorrection) {
        this(new LinearSmoothMode(), moveCorrection, true);
    }

    public RotationConfig(AngleSmoothMode angleSmooth, boolean moveCorrection, boolean freeCorrection) {
        this(angleSmooth, moveCorrection, freeCorrection, false);
    }

    public RotationConfig(AngleSmoothMode angleSmooth, boolean moveCorrection, boolean freeCorrection, boolean targetCorrection) {
        this.angleSmooth = angleSmooth;
        this.moveCorrection = moveCorrection;
        this.freeCorrection = freeCorrection;
        this.targetCorrection = targetCorrection;
    }

    public RotationPlan createRotationPlan(Angle angle, class_243 vec, class_1297 entity, int reset) {
        return new RotationPlan(angle, vec, entity, this.angleSmooth, reset, 3.0f, this.moveCorrection, this.freeCorrection, this.targetCorrection);
    }

    public RotationPlan createRotationPlan(Angle angle, class_243 vec, class_1297 entity, boolean moveCorrection, boolean freeCorrection) {
        return new RotationPlan(angle, vec, entity, this.angleSmooth, 1, 3.0f, moveCorrection, freeCorrection, false);
    }
}

