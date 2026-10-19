/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import ru.recoin.common.QuickImports;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;

public abstract class AngleSmoothMode
implements QuickImports {
    private final String name;

    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle) {
        return this.limitAngleChange(currentAngle, targetAngle, null, null);
    }

    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, class_243 vec3d) {
        return this.limitAngleChange(currentAngle, targetAngle, vec3d, null);
    }

    public abstract Angle limitAngleChange(Angle var1, Angle var2, class_243 var3, class_1297 var4);

    public abstract class_243 randomValue();

    public String getName() {
        return this.name;
    }

    public AngleSmoothMode(String name) {
        this.name = name;
    }
}

