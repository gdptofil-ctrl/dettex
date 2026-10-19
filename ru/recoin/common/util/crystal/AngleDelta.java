/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_241
 */
package ru.recoin.common.util.crystal;

import net.minecraft.class_241;

public class AngleDelta {
    private final float yaw;
    private final float pitch;

    public AngleDelta(float amount, float amount2) {
        this.yaw = amount;
        this.pitch = amount2;
    }

    public float getYaw() {
        return this.yaw;
    }

    public class_241 toVector() {
        return new class_241(this.yaw, this.pitch);
    }

    public boolean isWithin(float amount, float amount2) {
        return Math.abs(this.yaw) < amount && Math.abs(this.pitch) < amount2;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float length() {
        return (float)Math.sqrt(this.yaw * this.yaw + this.pitch * this.pitch);
    }

    public boolean isSmall(float amount) {
        return this.isWithin(amount, amount);
    }
}

