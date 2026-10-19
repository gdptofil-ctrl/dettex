/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 */
package ru.recoin.implement.features.modules.combat.aim;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_310;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;

public abstract class AimMode {
    protected final String name;
    protected float speed = 3.0f;
    protected static final class_310 mc = class_310.method_1551();

    protected AimMode(String name) {
        this.name = name;
    }

    public abstract Angle calculateRotation(Angle var1, Angle var2, class_243 var3, class_1297 var4);

    public String getName() {
        return this.name;
    }

    public float getSpeed() {
        return this.speed;
    }
}

