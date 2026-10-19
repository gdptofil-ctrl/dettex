/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation;

import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.RotationController;

public class Angle {
    public static Angle DEFAULT = new Angle(0.0f, 0.0f);
    private float yaw;
    private float pitch;

    public Angle adjustSensitivity() {
        double gcd = MathUtil.computeGcd();
        Angle previousAngle = RotationController.INSTANCE.getServerAngle();
        float adjustedYaw = this.adjustYaw(this.yaw, previousAngle.yaw, gcd);
        float adjustedPitch = this.adjustPitch(this.pitch, previousAngle.pitch, gcd);
        return new Angle(adjustedYaw, adjustedPitch);
    }

    public Angle random(float f) {
        return new Angle(this.yaw + MathUtil.getRandom(-f, f), this.pitch + MathUtil.getRandom(-f, f));
    }

    private float adjustYaw(float axisValue, float previousValue, double gcd) {
        double delta = class_3532.method_15393((float)(axisValue - previousValue));
        long mouseSteps = Math.round(delta / gcd);
        return (float)((double)previousValue + (double)mouseSteps * gcd);
    }

    private float adjustPitch(float axisValue, float previousValue, double gcd) {
        double target = class_3532.method_15363((float)axisValue, (float)-90.0f, (float)90.0f);
        long mouseSteps = Math.round((target - (double)previousValue) / gcd);
        double adjusted = (double)previousValue + (double)mouseSteps * gcd;
        if (adjusted > 90.0) {
            mouseSteps = (long)Math.floor((90.0 - (double)previousValue) / gcd);
        } else if (adjusted < -90.0) {
            mouseSteps = (long)Math.ceil((-90.0 - (double)previousValue) / gcd);
        }
        return (float)((double)previousValue + (double)mouseSteps * gcd);
    }

    public final class_243 toVector() {
        float f = this.pitch * ((float)Math.PI / 180);
        float g = -this.yaw * ((float)Math.PI / 180);
        float h = class_3532.method_15362((double)g);
        float i = class_3532.method_15374((double)g);
        float j = class_3532.method_15362((double)f);
        float k = class_3532.method_15374((double)f);
        return new class_243((double)(i * j), (double)(-k), (double)(h * j));
    }

    public Angle addYaw(float yaw) {
        return new Angle(this.yaw + yaw, this.pitch);
    }

    public Angle addPitch(float pitch) {
        this.pitch = class_3532.method_15363((float)(this.pitch + pitch), (float)-90.0f, (float)90.0f);
        return this;
    }

    public Angle of(Angle angle) {
        return new Angle(angle.getYaw(), angle.getPitch());
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String toString() {
        return "Angle(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
    }

    public Angle(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static class VecRotation {
        private final Angle angle;
        private final class_243 vec;

        public String toString() {
            return "Angle.VecRotation(angle=" + String.valueOf(this.getAngle()) + ", vec=" + String.valueOf(this.getVec()) + ")";
        }

        public Angle getAngle() {
            return this.angle;
        }

        public class_243 getVec() {
            return this.vec;
        }

        public VecRotation(Angle angle, class_243 vec) {
            this.angle = angle;
            this.vec = vec;
        }
    }
}

