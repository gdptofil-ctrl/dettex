/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  org.joml.Vector2f
 */
package ru.recoin.common.util.crystal;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import org.joml.Vector2f;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.crystal.AimQuantizer;
import ru.recoin.common.util.crystal.AngleDelta;
import ru.recoin.mixins.ClientPlayerEntityRotationAccessor;

public class AimAngles
implements QuickImports {
    private float yaw;
    private float pitch;

    private float wrapDifference(float amount, float amount2) {
        return class_3532.method_15393((float)(amount - amount2));
    }

    public AimAngles quantizeRelativeTo(AimAngles angle) {
        if (!this.equals(angle)) {
            AngleDelta delta = angle.differenceTo(this);
            double distance = AimQuantizer.getSensitivityScale();
            int count = (int)((double)delta.getYaw() / distance);
            int count2 = (int)((double)delta.getPitch() / distance);
            return new AimAngles((float)((double)angle.getYaw() + (double)count * distance), (float)((double)angle.getPitch() + (double)count2 * distance));
        }
        return this;
    }

    public static class_243 vectorFromPitchYaw(float amount, float amount2) {
        float amount3 = -amount2 * ((float)Math.PI / 180) - (float)Math.PI;
        float amount4 = -amount * ((float)Math.PI / 180);
        float amount5 = (float)Math.cos(amount3);
        float amount6 = (float)Math.sin(amount3);
        float amount7 = (float)(-Math.cos(amount4));
        float amount8 = (float)Math.sin(amount4);
        return new class_243((double)(amount6 * amount7), (double)amount8, (double)(amount5 * amount7));
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setYaw(float amount) {
        this.yaw = amount;
    }

    public AimAngles(class_1297 entity) {
        this.yaw = entity.method_36454();
        this.pitch = entity.method_36455();
    }

    protected boolean canEqual(Object owner) {
        return owner instanceof AimAngles;
    }

    public final class_243 toVector() {
        float amount = this.pitch * ((float)Math.PI / 180);
        float amount2 = -this.yaw * ((float)Math.PI / 180);
        float amount3 = class_3532.method_15362((double)amount2);
        float amount4 = class_3532.method_15374((double)amount2);
        float amount5 = class_3532.method_15362((double)amount);
        float amount6 = class_3532.method_15374((double)amount);
        return new class_243((double)(amount4 * amount5), (double)(-amount6), (double)(amount3 * amount5));
    }

    public AngleDelta differenceTo(AimAngles angle) {
        return new AngleDelta(this.wrapDifference(angle.yaw, this.yaw), this.wrapDifference(angle.pitch, this.pitch));
    }

    public AimAngles() {
    }

    public String toString() {
        return "Rotation(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
    }

    public int hashCode() {
        int value = 59;
        int count = 1;
        count = count * 59 + Float.floatToIntBits(this.getYaw());
        return count * 59 + Float.floatToIntBits(this.getPitch());
    }

    public static float getCameraPitch() {
        return (float)(AimAngles.mc.field_1773.method_19418().method_19333() ? -1 : 1) * AimAngles.mc.field_1773.method_19418().method_19329();
    }

    public static AimAngles lastSent() {
        return new AimAngles(((ClientPlayerEntityRotationAccessor)AimAngles.mc.field_1724).recoin$getLastYaw(), ((ClientPlayerEntityRotationAccessor)AimAngles.mc.field_1724).recoin$getLastPitch());
    }

    public static Vector2f getCameraAngles() {
        return new Vector2f(AimAngles.getCameraYaw(), AimAngles.getCameraPitch());
    }

    public static float clampPitch(float amount) {
        return class_3532.method_15363((float)amount, (float)-90.0f, (float)90.0f);
    }

    public float getYaw() {
        return this.yaw;
    }

    public static AimAngles subtract(AimAngles angle, AimAngles angle2) {
        float yaw = class_3532.method_15393((float)(angle2.getYaw() - angle.getYaw()));
        float yaw2 = class_3532.method_15393((float)(angle2.getPitch() - angle.getPitch()));
        return new AimAngles(yaw, yaw2);
    }

    public boolean equals(Object owner) {
        if (owner == this) {
            return true;
        }
        if (!(owner instanceof AimAngles)) {
            return false;
        }
        AimAngles angle = (AimAngles)owner;
        if (!angle.canEqual(this)) {
            return false;
        }
        return Float.compare(this.getYaw(), angle.getYaw()) != 0 ? false : Float.compare(this.getPitch(), angle.getPitch()) == 0;
    }

    public AimAngles(float amount, float amount2) {
        this.yaw = amount;
        this.pitch = amount2;
    }

    public float distanceTo(AimAngles angle) {
        float yaw2 = class_3532.method_15393((float)(angle.getYaw() - this.yaw));
        float pitch2 = angle.getPitch() - this.pitch;
        return (float)Math.hypot(Math.abs(yaw2), Math.abs(pitch2));
    }

    public void setPitch(float amount) {
        this.pitch = amount;
    }

    public static float getCameraYaw() {
        return class_3532.method_15393((float)(AimAngles.mc.field_1773.method_19418().method_19330() + (float)(AimAngles.mc.field_1773.method_19418().method_19333() ? 180 : 0)));
    }
}

