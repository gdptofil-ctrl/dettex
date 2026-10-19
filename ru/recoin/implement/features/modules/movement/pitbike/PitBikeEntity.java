/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1299
 *  net.minecraft.class_1313
 *  net.minecraft.class_1690
 *  net.minecraft.class_1802
 *  net.minecraft.class_1937
 *  net.minecraft.class_243
 *  net.minecraft.class_2940
 *  net.minecraft.class_2941
 *  net.minecraft.class_2943
 *  net.minecraft.class_2945
 *  net.minecraft.class_2945$class_9222
 *  net.minecraft.class_3532
 *  net.minecraft.class_4048
 */
package ru.recoin.implement.features.modules.movement.pitbike;

import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1313;
import net.minecraft.class_1690;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_2940;
import net.minecraft.class_2941;
import net.minecraft.class_2943;
import net.minecraft.class_2945;
import net.minecraft.class_3532;
import net.minecraft.class_4048;

public final class PitBikeEntity
extends class_1690 {
    private static final class_2940<Boolean> WHEELIE = class_2945.method_12791(PitBikeEntity.class, (class_2941)class_2943.field_13323);
    private static final class_2940<Integer> GEAR = class_2945.method_12791(PitBikeEntity.class, (class_2941)class_2943.field_13327);
    private static final float[] GEAR_LIMITS = new float[]{0.0f, 0.34f, 0.55f, 0.77f, 1.0f};
    private transient boolean throttleForward;
    private transient boolean throttleBack;
    private transient boolean steerLeft;
    private transient boolean steerRight;
    private transient double driveSpeed;
    private transient double motionX;
    private transient double motionZ;
    private transient float topSpeed = 0.75f;
    private transient float acceleration = 0.018f;
    private transient float wheelieAngle;
    private transient float previousWheelieAngle;
    private transient float steeringAngle;
    private transient float previousSteeringAngle;
    private transient float leanAngle;
    private transient float previousLeanAngle;
    private transient float wheelPhase;
    private transient float previousWheelPhase;
    private transient float terrainPitch;
    private transient float previousTerrainPitch;
    private transient float suspension;
    private transient float previousSuspension;
    private transient float suspensionVelocity;
    private transient float engineRpm = 1500.0f;
    private transient double airborneVelocity;
    private transient int shiftTicks;
    private transient boolean manualTransmission = true;

    public PitBikeEntity(class_1299<? extends class_1690> type, class_1937 world) {
        super(type, world, () -> class_1802.field_8162);
    }

    protected void method_5693(class_2945.class_9222 builder) {
        super.method_5693(builder);
        builder.method_56912(WHEELIE, false);
        builder.method_56912(GEAR, 1);
    }

    public void setDriveSettings(float speed, float acceleration) {
        this.topSpeed = class_3532.method_15363((float)speed, (float)0.2f, (float)1.2f);
        this.acceleration = class_3532.method_15363((float)acceleration, (float)0.005f, (float)0.04f);
    }

    public void setWheelieRequested(boolean requested) {
        this.field_6011.method_12778(WHEELIE, requested);
    }

    public void setManualTransmission(boolean enabled) {
        this.manualTransmission = enabled;
    }

    public boolean isManualTransmission() {
        return this.manualTransmission;
    }

    public int getGear() {
        return (Integer)this.field_6011.method_12789(GEAR);
    }

    public void setGear(int gear) {
        int next = class_3532.method_15340((int)gear, (int)0, (int)4);
        if (next == this.getGear()) {
            return;
        }
        this.field_6011.method_12778(GEAR, next);
        this.shiftTicks = 3;
    }

    public boolean canShift() {
        return this.shiftTicks == 0;
    }

    public void method_64487(boolean left, boolean right, boolean forward, boolean back) {
        this.steerLeft = left;
        this.steerRight = right;
        this.throttleForward = forward;
        this.throttleBack = back;
        super.method_64487(false, false, false, false);
    }

    protected int method_64500() {
        return 1;
    }

    protected double method_64489(class_4048 dimensions) {
        return 0.82 + Math.sin(Math.toRadians(this.wheelieAngle + this.terrainPitch)) * 0.7 - (double)this.suspension;
    }

    public void method_5773() {
        double traveled;
        float desiredRpm;
        boolean driving;
        this.previousWheelieAngle = this.wheelieAngle;
        this.previousSteeringAngle = this.steeringAngle;
        this.previousLeanAngle = this.leanAngle;
        this.previousWheelPhase = this.wheelPhase;
        this.previousTerrainPitch = this.terrainPitch;
        this.previousSuspension = this.suspension;
        double previousX = this.method_23317();
        double previousZ = this.method_23321();
        if (this.shiftTicks > 0) {
            --this.shiftTicks;
        }
        int gear = this.getGear();
        double gearLimit = this.manualTransmission ? (double)(this.topSpeed * GEAR_LIMITS[gear]) : (double)this.topSpeed;
        double torque = this.manualTransmission ? 1.5 - (double)gear * 0.2 : 1.0;
        boolean bl = driving = this.method_66247() && !this.method_5799();
        if (driving) {
            if (!this.method_5782()) {
                this.steerRight = false;
                this.steerLeft = false;
                this.throttleBack = false;
                this.throttleForward = false;
                this.driveSpeed *= 0.78;
            } else {
                this.driveSpeed = this.method_24828() ? (this.throttleBack && this.driveSpeed > 0.0 ? Math.max(0.0, this.driveSpeed - 0.045) : (this.throttleForward && !this.throttleBack && gearLimit > 0.0 && this.shiftTicks == 0 ? (this.driveSpeed > gearLimit ? Math.max(gearLimit, this.driveSpeed - 0.018) : Math.min(gearLimit, this.driveSpeed + (double)this.acceleration * torque * (1.0 - 0.55 * Math.max(0.0, this.driveSpeed) / gearLimit))) : (this.throttleBack && !this.throttleForward && !this.manualTransmission ? Math.max(-0.14, this.driveSpeed - (double)this.acceleration * 0.4) : (this.driveSpeed *= 0.97)))) : (this.driveSpeed *= 0.998);
            }
            if (Math.abs(this.driveSpeed) < 0.002) {
                this.driveSpeed = 0.0;
            }
            int steering = (this.steerRight ? 1 : 0) - (this.steerLeft ? 1 : 0);
            float turn = (float)steering * (float)Math.min(Math.abs(this.driveSpeed) * 12.0, 3.5) * (this.driveSpeed < 0.0 ? -1.0f : 1.0f) * (this.wheelieAngle > 10.0f ? 0.45f : 1.0f) * (this.method_24828() ? 1.0f : 0.12f);
            this.method_36456(this.method_36454() + turn);
            this.steeringAngle += ((float)steering * 25.0f - this.steeringAngle) * 0.3f;
            this.leanAngle += ((float)(-steering) * (float)Math.min(Math.abs(this.driveSpeed) * 22.0, 14.0) - this.leanAngle) * 0.2f;
        }
        float desiredWheelie = this.method_5782() && (Boolean)this.field_6011.method_12789(WHEELIE) != false && this.method_24828() && (driving ? this.driveSpeed > 0.1 : this.method_18798().method_37267() > 0.1) ? 38.0f : 0.0f;
        this.wheelieAngle += class_3532.method_15363((float)(desiredWheelie - this.wheelieAngle), (float)-5.0f, (float)3.0f);
        double speedRatio = Math.min(1.2, Math.abs(this.driveSpeed) / Math.max(0.05, gearLimit));
        desiredRpm = this.method_5782() ? Math.min(9500.0f, 1500.0f + (float)speedRatio * 5800.0f + (this.throttleForward ? 1200.0f : 0.0f) + (!this.method_24828() && this.throttleForward ? 900.0f : 0.0f)) : 0.0f;
        if (this.manualTransmission && gear == 0) {
            desiredRpm = this.method_5782() ? (this.throttleForward ? 6500.0f : 1500.0f) : 0.0f;
        }
        this.engineRpm += (desiredRpm - this.engineRpm) * 0.15f;
        super.method_5773();
        if (this.method_24828() && this.airborneVelocity < -0.12) {
            this.suspensionVelocity += (float)Math.min(0.1, -this.airborneVelocity * 0.12);
            if (driving) {
                this.driveSpeed *= Math.max(0.8, 1.0 + this.airborneVelocity * 0.08);
            }
        }
        this.airborneVelocity = this.method_24828() ? 0.0 : this.method_18798().field_1351;
        this.suspensionVelocity = (this.suspensionVelocity - this.suspension * 0.35f) * 0.68f;
        this.suspension = class_3532.method_15363((float)(this.suspension + this.suspensionVelocity), (float)-0.035f, (float)0.12f);
        float desiredPitch = this.method_24828() ? 0.0f : (float)class_3532.method_15350((double)(this.method_18798().field_1351 * 30.0), (double)-20.0, (double)20.0);
        this.terrainPitch += (desiredPitch - this.terrainPitch) * 0.2f;
        if (driving && this.field_5976) {
            this.driveSpeed *= 0.25;
        }
        if ((traveled = Math.hypot(this.method_23317() - previousX, this.method_23321() - previousZ)) < 2.0) {
            this.wheelPhase += (float)(traveled / 0.34) * (this.driveSpeed < 0.0 ? -1.0f : 1.0f);
        }
        if (!this.method_5782() && !this.method_73183().method_8608()) {
            this.setWheelieRequested(false);
        }
    }

    public void method_5784(class_1313 type, class_243 movement) {
        boolean grounded = this.method_24828();
        double previousY = this.method_23318();
        if (type == class_1313.field_6308 && this.method_66247() && !this.method_5799()) {
            double yaw = Math.toRadians(this.method_36454());
            float slipperiness = this.method_73183().method_8320(this.method_23314()).method_26204().method_9499();
            double grip = grounded ? class_3532.method_15350((double)(0.27 / (double)slipperiness), (double)0.15, (double)0.6) : 0.025;
            this.motionX += (-Math.sin(yaw) * this.driveSpeed - this.motionX) * grip;
            this.motionZ += (Math.cos(yaw) * this.driveSpeed - this.motionZ) * grip;
            movement = new class_243(this.motionX, movement.field_1351, this.motionZ);
            this.method_18799(movement);
        }
        super.method_5784(type, movement);
        if (this.field_5976) {
            this.motionX = this.method_18798().field_1352;
            this.motionZ = this.method_18798().field_1350;
        }
        double climbed = this.method_23318() - previousY;
        if (type == class_1313.field_6308 && grounded && this.method_66247() && climbed > 0.12 && climbed <= 1.06 && Math.abs(this.driveSpeed) > 0.38 && !this.method_5799()) {
            double launch = Math.min(0.62, 0.1 + Math.abs(this.driveSpeed) * 0.42 + climbed * 0.08);
            if (this.method_73183().method_8587((class_1297)this, this.method_5829().method_989(0.0, launch, 0.0))) {
                this.method_18800(this.method_18798().field_1352, launch, this.method_18798().field_1350);
                this.method_24830(false);
                this.suspensionVelocity = -0.03f;
                this.driveSpeed *= 0.95;
            }
        }
    }

    public float method_49476() {
        return Math.abs(this.driveSpeed) > 0.02 ? 1.05f : 0.6f;
    }

    protected double method_7490() {
        return this.method_5799() ? super.method_7490() : 0.08;
    }

    public float getTerrainPitch(float delta) {
        return class_3532.method_16439((float)delta, (float)this.previousTerrainPitch, (float)this.terrainPitch);
    }

    public float getSuspension(float delta) {
        return class_3532.method_16439((float)delta, (float)this.previousSuspension, (float)this.suspension);
    }

    public float getEngineRpm() {
        return this.engineRpm;
    }

    public float getThrottle() {
        return this.throttleForward ? 1.0f : 0.0f;
    }

    public float getWheelieAngle(float delta) {
        return class_3532.method_16439((float)delta, (float)this.previousWheelieAngle, (float)this.wheelieAngle);
    }

    public float getSteeringAngle(float delta) {
        return class_3532.method_16439((float)delta, (float)this.previousSteeringAngle, (float)this.steeringAngle);
    }

    public float getLeanAngle(float delta) {
        return class_3532.method_16439((float)delta, (float)this.previousLeanAngle, (float)this.leanAngle);
    }

    public float getWheelPhase(float delta) {
        return class_3532.method_16439((float)delta, (float)this.previousWheelPhase, (float)this.wheelPhase);
    }
}

