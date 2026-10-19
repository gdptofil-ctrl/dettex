/*
 * "New Sloth" rotation.
 *
 * Ported from the imported ru.white ShardRotation and adapted to Recoin's
 * per-tick AngleSmoothMode contract. Kept intact:
 *  - wandering multipoint (local offset from the target feet, clamped to the
 *    hitbox) so the hit point drifts like a human hand;
 *  - accelerating/decelerating speed oscillation with periodic pauses/activity;
 *  - smoothed velocity with gaussian motor noise and per-axis gain scaling;
 *  - mouse-step (GCD) quantization so every delta is a valid sensitivity step;
 *  - tiny drift and breathing so the aim is never perfectly linear.
 *
 * Improvements over the imported source:
 *  - no event subscription / global RotationProcess / FreeLook coupling;
 *  - internal state is seeded from the rotation actually applied by the
 *    controller instead of the raw player yaw (no first-tick snap);
 *  - the discarded wander jitter is now applied correctly and the point is
 *    clamped to the box (the original could leave the hitbox);
 *  - attack readiness is derived from the vanilla cooldown + Aura reach;
 *  - configurable speed multiplier;
 *  - dead elytra prediction / overshoot stubs removed.
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;

public class NewSlothSmoothMode
extends AngleSmoothMode {
    private static final float PITCH_YAW_RATIO = 0.836f;

    /*
     * The imported source ran from a render event (per frame); Recoin updates
     * rotations on the 20 Hz tick thread. Scaling the speed oscillation gains
     * by 3 keeps the wall-clock accelerate/decelerate cadence of the original.
     */
    private static final float TICK_SCALE = 3.0f;

    private final ThreadLocalRandom random = ThreadLocalRandom.current();
    private final Multiplier multiplier = new Multiplier();

    private class_1297 currentTarget;
    private boolean initialized;
    private float serverYaw;
    private float serverPitch;
    private float internalYaw;
    private float internalPitch;
    private float velYawSm;
    private float velPitchSm;
    private float prevAppliedYawVel;
    private float prevAppliedPitchVel;
    private float lastYawAccel;
    private float speedOsc;
    private boolean accelerating;
    private float noiseSm;
    private int tickCounter;
    private float accelPeak;
    private float decelFloor;
    private float peakMul;
    private int pauseTicks;
    private float activity = 1.0f;
    private float yawJitterSm;
    private float pitchJitterSm;
    private float drift;
    private final int[] periods = new int[]{16, 21};
    private int periodIndex;
    private long periodExpiry;
    private class_243 wanderOffset = class_243.field_1353;
    private class_243 wanderVel = class_243.field_1353;

    public NewSlothSmoothMode() {
        super("New Sloth");
        this.reshuffleAll();
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 vec, class_1297 entity) {
        if (current == null) {
            current = AngleUtil.cameraAngle();
        }
        if (target == null) {
            target = current;
        }
        if (NewSlothSmoothMode.mc.field_1724 == null || NewSlothSmoothMode.mc.field_1687 == null) {
            return current;
        }

        boolean hasTarget = entity instanceof class_1309;
        class_1309 living = hasTarget ? (class_1309)entity : null;
        Aura aura = Aura.getInstance();

        if (!this.initialized) {
            this.seedFrom(current);
        }
        if (hasTarget && this.currentTarget != entity) {
            this.currentTarget = entity;
            this.speedOsc = this.random.nextFloat() * 0.05f;
            this.accelerating = false;
            this.tickCounter = 0;
            this.accelPeak = 0.18f + this.random.nextFloat() * 0.1f;
            this.decelFloor = -0.02f - this.random.nextFloat() * 0.05f;
            this.peakMul = 0.8f + this.random.nextFloat() * 0.4f;
            this.seedFrom(current);
            this.pauseTicks = 0;
            this.activity = 1.0f;
        } else if (!hasTarget) {
            this.currentTarget = null;
        }

        ++this.tickCounter;
        this.noiseSm = this.noiseSm * 0.85f + (float)this.random.nextGaussian() * 0.045f;
        boolean windowClosed = System.currentTimeMillis() % 500L >= 370L;
        float gate = windowClosed ? 0.0f : 1.0f;

        float aimYaw;
        float aimPitch;
        if (hasTarget) {
            class_243 point = this.getMultipoint(living);
            if (point == null) {
                aimYaw = target.getYaw();
                aimPitch = target.getPitch();
            } else {
                Angle aim = AngleUtil.fromVec3d(point.method_1020(NewSlothSmoothMode.mc.field_1724.method_33571()));
                aimYaw = aim.getYaw();
                aimPitch = aim.getPitch();
            }
        } else {
            aimYaw = target.getYaw();
            aimPitch = target.getPitch();
        }

        float yawDist = Math.abs(class_3532.method_15393(aimYaw - this.serverYaw));
        boolean ready = hasTarget && NewSlothSmoothMode.mc.field_1724.method_7261(1.0f) > 0.9f;
        if (ready && aura != null) {
            double reach = (double)aura.getAttackDistance() + 1.5;
            ready = NewSlothSmoothMode.mc.field_1724.method_33571().method_1025(living.method_33571()) <= reach * reach;
        }

        if (!this.accelerating) {
            float gain = 0.0055f;
            gain += (0.004f + 0.012f * (1.0f - (float)Math.exp(-yawDist / 35.0f))) * 1.8f;
            gain += this.random.nextFloat() * 0.0025f;
            if (ready) {
                gain += 0.012857143f;
            }
            this.speedOsc += gain * TICK_SCALE * (1.6f + this.noiseSm);
            if (this.speedOsc >= this.accelPeak) {
                this.accelerating = true;
                this.decelFloor = -0.02f - this.random.nextFloat() * 0.05f;
            }
        } else {
            float drop = (ready ? 0.045f : 0.008f) * (0.85f + this.random.nextFloat() * 0.3f);
            this.speedOsc -= drop * TICK_SCALE * (2.1f + this.noiseSm);
            if (this.speedOsc <= this.decelFloor) {
                this.accelerating = false;
                this.accelPeak = 0.18f + this.random.nextFloat() * 0.1f;
            }
        }

        boolean gliding = NewSlothSmoothMode.mc.field_1724.method_6128();
        float stepCap = class_3532.method_15363(this.speedOsc, 0.0f, gliding ? 0.456f : 0.312f);
        if (ready) {
            stepCap = Math.min(stepCap + 0.1f, gliding ? 0.552f : 0.408f);
        }
        stepCap += this.noiseSm * 0.25f;
        if (this.tickCounter <= 3) {
            stepCap *= 0.2f * (float)this.tickCounter * this.peakMul;
        }

        float dYaw = class_3532.method_15393(aimYaw - this.internalYaw);
        float dPitch = aimPitch - this.internalPitch;

        if (this.pauseTicks > 0) {
            --this.pauseTicks;
        } else if (this.random.nextFloat() < 0.03f && this.tickCounter > 4 && !ready && yawDist < 10.0f) {
            this.pauseTicks = this.random.nextInt(3) + 2;
        }
        if (this.pauseTicks > 0) {
            this.activity += (0.0f - this.activity) * 0.35f;
        } else {
            this.activity += (1.0f - this.activity) * 0.35f;
        }

        float randScale = 0.9f + this.random.nextFloat() * 0.2f;
        float speedMul = this.getSpeedMultiplier();
        float maxYawStep = (gliding ? 86.4f : (ready ? 38.4f : 28.8f)) * randScale * speedMul;
        float maxPitchStep = (gliding ? 14.4f : (ready ? 5.4f : 3.36f)) * randScale * speedMul;
        dYaw = this.smoothClamp(dYaw, maxYawStep);
        dPitch = this.smoothClamp(dPitch, maxPitchStep);

        float moveYaw = stepCap * (0.94f + this.noiseSm * 0.92f) * gate;
        float movePitch = Math.min(stepCap * PITCH_YAW_RATIO, moveYaw * 0.96f) * gate;

        float wantVelYaw = dYaw * moveYaw * this.activity;
        float wantVelPitch = dPitch * movePitch * this.activity;
        float kYawBase = 0.16f + this.random.nextFloat() * 0.08f;
        float kPitchBase = 0.45f + this.random.nextFloat() * 0.15f;
        float adYaw = Math.abs(dYaw);
        float adPitch = Math.abs(dPitch);
        float kYawScale = adYaw < 3.0f ? 0.4f + 0.6f * (adYaw / 3.0f) : 1.0f;
        float kPitchScale = adPitch < 3.0f ? 0.4f + 0.6f * (adPitch / 3.0f) : 1.0f;
        float kYaw = kYawBase * kYawScale;
        float kPitch = kPitchBase * kPitchScale;
        float yawSigma = 0.006f + Math.abs(this.velYawSm) * 0.012f;
        float pitchSigma = 0.02f + Math.abs(this.velPitchSm) * 0.05f;
        this.yawJitterSm = this.yawJitterSm * 0.55f + (float)this.random.nextGaussian() * yawSigma * 0.45f;
        this.pitchJitterSm = this.pitchJitterSm * 0.55f + (float)this.random.nextGaussian() * pitchSigma * 0.45f;
        float accelYaw = (wantVelYaw - this.velYawSm) * kYaw + this.yawJitterSm;
        float accelBound = 0.7f + Math.abs(this.velYawSm) * 0.2f;
        accelYaw = class_3532.method_15363(accelYaw, this.lastYawAccel - accelBound, this.lastYawAccel + accelBound);
        this.velYawSm += accelYaw;
        this.lastYawAccel = accelYaw;
        this.velPitchSm += (wantVelPitch - this.velPitchSm) * kPitch + this.pitchJitterSm;
        this.velPitchSm = this.smoothClamp(this.velPitchSm, 4.8f);

        float appliedYawVel = (this.velYawSm + this.prevAppliedYawVel) * 0.5f;
        float appliedPitchVel = (this.velPitchSm + this.prevAppliedPitchVel) * 0.5f;
        this.prevAppliedYawVel = this.velYawSm;
        this.prevAppliedPitchVel = this.velPitchSm;
        this.internalYaw += appliedYawVel;
        this.internalPitch = class_3532.method_15363(this.internalPitch + appliedPitchVel, -89.0f, 89.0f);

        float mult = hasTarget ? this.multiplier.getMultiplier(living) : 1.0f;
        this.internalYaw += this.breath(1.1f * mult * gate, 18.0f);
        this.internalPitch += this.breath(0.05f * mult * gate, this.nextPeriod());

        double gcd = MathUtil.computeGcd();
        float outYaw;
        float outPitch;
        if (gcd > 0.0) {
            double deltaYaw = (double)this.internalYaw - (double)this.serverYaw;
            double deltaPitch = (double)this.internalPitch - (double)this.serverPitch;
            deltaPitch = (double)this.smoothClamp((float)deltaPitch, 4.8f);
            long stepsYaw = Math.round(deltaYaw / gcd);
            long stepsPitch = Math.round(deltaPitch / gcd);
            outYaw = this.serverYaw + (float)((double)stepsYaw * gcd);
            outPitch = this.serverPitch + (float)((double)stepsPitch * gcd);
        } else {
            outYaw = this.internalYaw;
            outPitch = this.internalPitch;
        }
        outPitch = class_3532.method_15363(outPitch, -89.0f, 89.0f);

        if (gcd > 0.0) {
            if (Math.abs(class_3532.method_15393(this.internalYaw - outYaw)) > gcd * 3.0) {
                this.internalYaw += (outYaw - this.internalYaw) * 0.5f;
                this.velYawSm *= 0.4f;
            }
            if (Math.abs(this.internalPitch - outPitch) > gcd * 3.0) {
                this.internalPitch += (outPitch - this.internalPitch) * 0.5f;
                this.velPitchSm *= 0.4f;
            }
        }

        this.drift += (float)(this.random.nextGaussian() * 2.0E-5);
        this.drift = this.drift >= 0.0f
            ? class_3532.method_15363(this.drift, 7.0E-4f, 0.0016f)
            : class_3532.method_15363(this.drift, -0.0016f, -7.0E-4f);
        float rawDeltaYaw = outYaw - this.serverYaw;
        float sendYaw = this.serverYaw + rawDeltaYaw * (1.0f + this.drift);

        this.serverYaw = sendYaw;
        this.serverPitch = outPitch;
        return new Angle(sendYaw, outPitch);
    }

    private void seedFrom(Angle current) {
        this.serverYaw = current.getYaw();
        this.serverPitch = current.getPitch();
        this.internalYaw = current.getYaw();
        this.internalPitch = current.getPitch();
        this.velYawSm = 0.0f;
        this.velPitchSm = 0.0f;
        this.prevAppliedYawVel = 0.0f;
        this.prevAppliedPitchVel = 0.0f;
        this.lastYawAccel = 0.0f;
        this.initialized = true;
    }

    private class_243 getMultipoint(class_1309 entity) {
        class_238 box = entity.method_5829();
        double lenX = Math.max(0.16, box.method_17939());
        double lenY = Math.max(0.32, box.method_17940());
        double lenZ = Math.max(0.16, box.method_17941());
        double minX = 0.005;
        double maxX = 0.015;
        double minY = 0.0015;
        double maxY = 0.015;

        if (this.wanderVel.method_1027() == 0.0) {
            this.wanderVel = new class_243(this.randomRange(-0.02, 0.02), this.randomRange(-0.02, 0.02), this.randomRange(-0.02, 0.02));
        }
        if (this.wanderOffset.method_1027() == 0.0) {
            this.wanderOffset = new class_243(0.0, lenY * 0.5, 0.0);
        }
        this.wanderOffset = this.wanderOffset.method_1019(this.wanderVel);

        double xLim = Math.max(0.0, Math.min((lenX - 0.3) / 2.0, 0.18));
        double zLim = Math.max(0.0, Math.min((lenZ - 0.3) / 2.0, 0.18));
        if (this.wanderOffset.field_1352 >= xLim) {
            this.wanderVel = new class_243(-this.randomRange(minX, maxX), this.wanderVel.field_1351, this.wanderVel.field_1350);
        } else if (this.wanderOffset.field_1352 <= -xLim) {
            this.wanderVel = new class_243(this.randomRange(minX, maxX), this.wanderVel.field_1351, this.wanderVel.field_1350);
        }
        if (this.wanderOffset.field_1351 >= lenY * 0.75) {
            this.wanderVel = new class_243(this.wanderVel.field_1352, -this.randomRange(minY, maxY), this.wanderVel.field_1350);
        } else if (this.wanderOffset.field_1351 <= lenY * 0.3) {
            this.wanderVel = new class_243(this.wanderVel.field_1352, this.randomRange(minY, maxY), this.wanderVel.field_1350);
        }
        if (this.wanderOffset.field_1350 >= zLim) {
            this.wanderVel = new class_243(this.wanderVel.field_1352, this.wanderVel.field_1351, -this.randomRange(minX, maxX));
        } else if (this.wanderOffset.field_1350 <= -zLim) {
            this.wanderVel = new class_243(this.wanderVel.field_1352, this.wanderVel.field_1351, this.randomRange(minX, maxX));
        }

        double offsetX = clampDouble(this.wanderOffset.field_1352, -xLim, xLim);
        double offsetY = clampDouble(this.wanderOffset.field_1351, lenY * 0.3, lenY * 0.75);
        double offsetZ = clampDouble(this.wanderOffset.field_1350, -zLim, zLim);
        this.wanderOffset = new class_243(offsetX, offsetY, offsetZ);

        return new class_243(entity.method_23317() + offsetX, entity.method_23318() + offsetY, entity.method_23321() + offsetZ);
    }

    private float smoothClamp(float value, float maxStep) {
        if (maxStep <= 0.0f) {
            return 0.0f;
        }
        float sign = Math.signum(value);
        float abs = Math.abs(value);
        float outer = maxStep * (0.85f + this.random.nextFloat() * 0.3f);
        float inner = outer * (0.5f + this.random.nextFloat() * 0.15f);
        if (abs <= inner) {
            return value;
        }
        float over = abs - inner;
        float range = outer - inner;
        float mid = range * (over / (over + range));
        float extra = Math.min(over * 0.1f, range * 0.8f);
        return sign * (inner + mid + extra);
    }

    private float breath(float amplitude, float periodDivisor) {
        float time = (float)(System.currentTimeMillis() % 1000L) / periodDivisor;
        return (float)Math.sin(time) * amplitude;
    }

    private void reshuffleAll() {
        this.currentTarget = null;
        this.initialized = false;
        this.internalYaw = 0.0f;
        this.internalPitch = 0.0f;
        this.velYawSm = 0.0f;
        this.velPitchSm = 0.0f;
        this.prevAppliedYawVel = 0.0f;
        this.prevAppliedPitchVel = 0.0f;
        this.lastYawAccel = 0.0f;
        this.speedOsc = 0.0f;
        this.accelerating = false;
        this.noiseSm = 0.0f;
        this.tickCounter = 0;
        this.pauseTicks = 0;
        this.activity = 1.0f;
        this.yawJitterSm = 0.0f;
        this.pitchJitterSm = 0.0f;
        this.wanderOffset = class_243.field_1353;
        this.wanderVel = class_243.field_1353;
        this.drift = (this.random.nextBoolean() ? 1.0f : -1.0f) * (0.001f + this.random.nextFloat() * 4.0E-4f);
        this.accelPeak = 0.18f + this.random.nextFloat() * 0.1f;
        this.decelFloor = -0.02f - this.random.nextFloat() * 0.05f;
        this.peakMul = 0.8f + this.random.nextFloat() * 0.4f;
        this.reshufflePeriods();
    }

    private void reshufflePeriods() {
        this.shufflePeriods();
        this.periodIndex = 0;
        this.periodExpiry = System.currentTimeMillis() + 6000L;
    }

    private float nextPeriod() {
        long now = System.currentTimeMillis();
        while (now >= this.periodExpiry) {
            int first = this.periods[this.periodIndex];
            ++this.periodIndex;
            if (this.periodIndex == this.periods.length) {
                this.shufflePeriods();
                if (this.periods[0] == first) {
                    int swap = this.random.nextInt(1, this.periods.length);
                    int tmp = this.periods[0];
                    this.periods[0] = this.periods[swap];
                    this.periods[swap] = tmp;
                }
                this.periodIndex = 0;
            }
            this.periodExpiry += 6000L;
        }
        return (float)this.periods[this.periodIndex];
    }

    private void shufflePeriods() {
        for (int i = this.periods.length - 1; i > 0; --i) {
            int j = this.random.nextInt(i + 1);
            int tmp = this.periods[i];
            this.periods[i] = this.periods[j];
            this.periods[j] = tmp;
        }
    }

    private float getSpeedMultiplier() {
        Aura aura = Aura.getInstance();
        return aura != null ? Math.max(0.1f, aura.getNewSlothSpeed()) : 1.0f;
    }

    private double randomRange(double min, double max) {
        return this.random.nextDouble() * (max - min) + min;
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }

    private final class Multiplier {
        private float getMultiplier(class_1309 target) {
            class_243 eyes = NewSlothSmoothMode.mc.field_1724.method_33571();
            class_238 box = target.method_5829();
            double closestX = class_3532.method_15350(eyes.field_1352, box.field_1323, box.field_1320);
            double closestZ = class_3532.method_15350(eyes.field_1350, box.field_1321, box.field_1324);
            return Math.hypot(eyes.field_1352 - closestX, eyes.field_1350 - closestZ) <= 0.9 ? 0.93f : 1.0f;
        }
    }
}
