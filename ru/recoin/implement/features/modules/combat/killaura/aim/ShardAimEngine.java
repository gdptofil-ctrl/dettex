/*
 * Humanised aim-assist rotation for Shard-style anti-cheats.
 *
 * The engine never snaps to the target: every frame it moves the camera by a
 * small, quantised step (a multiple of the vanilla mouse sensitivity step),
 * limits acceleration, adds a slow noise wave and a randomised multi-point
 * offset, and only starts moving after a short reaction delay. This keeps the
 * rotation stream looking like a player moving the mouse instead of a
 * teleporting camera.
 */
package ru.recoin.implement.features.modules.combat.killaura.aim;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.math.MathUtil;

public final class ShardAimEngine
implements QuickImports {
    /** Hard per-frame caps so a single frame can never look like a snap. */
    private static final double MAX_YAW_STEP = 7.0;
    private static final double MAX_PITCH_STEP = 4.5;

    private class_1297 target;
    private long acquiredAt;
    private long readyAt;
    private long acquireMs;
    private long lastNanos;
    private long lastApplyNanos;
    private long nextPointAt;
    private long nextHeightAt;

    private double previousYawStep;
    private double previousPitchStep;
    private double pointX;
    private double pointY;
    private double pointZ;
    private double pointTargetX;
    private double pointTargetY;
    private double pointTargetZ;
    private double pointLerp;
    private double heightOffset;
    private double yawPhase;
    private double pitchPhase;
    private double yawPhaseSpeed;
    private double pitchPhaseSpeed;
    private double curvePower;
    private double speedScale;
    private double closeBoost;
    private int easingMode;

    public void reset() {
        this.target = null;
        this.acquiredAt = 0L;
        this.readyAt = 0L;
        this.lastNanos = 0L;
        this.lastApplyNanos = 0L;
        this.previousYawStep = 0.0;
        this.previousPitchStep = 0.0;
        this.pointX = this.pointY = this.pointZ = 0.0;
        this.nextPointAt = 0L;
        this.nextHeightAt = 0L;
        this.randomizeProfile();
    }

    public boolean hasTarget() {
        return this.target != null && this.target.method_5805();
    }

    /**
     * @param speed        base follow speed (roughly the Aura "Legit" speed range)
     * @param aimHeight    fraction of the hitbox height that is aimed at
     * @param onlyYaw      keep the pitch almost still (good for sword fights)
     * @param multipoint   wander between points on the hitbox instead of locking the centre
     * @param micro        micro-movements, reaction delay and noise
     */
    public void update(class_1297 entity, double speed, double aimHeight, boolean onlyYaw, boolean multipoint, boolean micro) {
        if (ShardAimEngine.mc.field_1724 == null || entity == null) {
            if (this.target != null) {
                this.reset();
            }
            return;
        }
        long nowNanos = System.nanoTime();
        if (this.lastApplyNanos != 0L && nowNanos - this.lastApplyNanos < 8_000_000L) {
            return;   // high-polling mice fire many events per frame: never apply more than ~2 steps per frame
        }
        this.lastApplyNanos = nowNanos;
        double frameScale = this.lastNanos > 0L ? (double)(nowNanos - this.lastNanos) / 1.66666667E7 : 1.0;
        frameScale = class_3532.method_15350(frameScale, 0.25, 3.0);
        this.lastNanos = nowNanos;

        long now = System.currentTimeMillis();
        if (entity != this.target) {
            this.target = entity;
            this.acquiredAt = now;
            this.acquireMs = micro ? 320L + ThreadLocalRandom.current().nextLong(220L) : 220L;
            this.readyAt = now + (micro ? 70L + ThreadLocalRandom.current().nextLong(110L) : 40L);
            this.randomizeProfile();
        }
        if (now < this.readyAt) {
            return;   // human reaction time before the hand starts moving
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (micro && now >= this.nextHeightAt) {
            this.heightOffset = class_3532.method_15350(this.heightOffset + random.nextDouble(-0.035, 0.035), -0.12, 0.1);
            this.nextHeightAt = now + 140L + random.nextLong(280L);
        }
        if (multipoint) {
            if (now >= this.nextPointAt) {
                this.randomizePoint(random);
            }
            double lerp = this.pointLerp + (micro ? random.nextDouble(-0.004, 0.004) : 0.0);
            this.pointX += (this.pointTargetX - this.pointX) * lerp;
            this.pointY += (this.pointTargetY - this.pointY) * lerp;
            this.pointZ += (this.pointTargetZ - this.pointZ) * lerp;
        }

        double[] desired = this.calculateAngles(entity, aimHeight, multipoint, micro);
        float currentYaw = ShardAimEngine.mc.field_1724.method_36454();
        float currentPitch = ShardAimEngine.mc.field_1724.method_36455();
        double yawDelta = this.wrapDegrees(desired[0] - (double)currentYaw);
        double pitchDelta = desired[1] - (double)currentPitch;
        if (onlyYaw) {
            double deadZone = micro ? 8.0 + random.nextDouble(4.0) : 10.0;
            if (Math.abs(pitchDelta) > deadZone) {
                double pull = micro ? 0.06 + random.nextDouble(0.06) : 0.08;
                pitchDelta = (pitchDelta - Math.signum(pitchDelta) * deadZone) * pull;
            } else {
                pitchDelta = 0.0;
            }
        }
        double difference = onlyYaw ? Math.abs(yawDelta) : Math.hypot(yawDelta, pitchDelta);
        double deadZone = micro ? 0.18 + random.nextDouble(0.14) : 0.25;
        if (difference < deadZone) {
            this.previousYawStep *= 0.7;
            this.previousPitchStep *= 0.7;
            return;   // already on target: do not twitch
        }

        double progress = class_3532.method_15350((double)(now - this.acquiredAt) / (double)Math.max(1L, this.acquireMs), 0.0, 1.0);
        double curve = this.easing(progress, micro);
        double baseSpeed = speed * (micro ? this.speedScale : 1.0);
        double differenceScale = micro
                ? (difference < 3.0 ? 0.32 + random.nextDouble(0.22) : difference < 10.0 ? 0.65 + random.nextDouble(0.3) : difference < 25.0 ? 0.85 + random.nextDouble(0.3) : difference < 50.0 ? 1.15 + random.nextDouble(0.35) : 1.4 + random.nextDouble(0.4))
                : (difference < 3.0 ? 0.45 : difference < 10.0 ? 0.85 : difference < 25.0 ? 1.0 : difference < 50.0 ? 1.3 : 1.6);
        double strength = baseSpeed * curve * differenceScale * frameScale;
        if (ShardAimEngine.mc.field_1724.field_6017 > 0.0) {
            double falling = micro ? 0.12 + random.nextDouble(0.08) : 0.15;
            strength *= 1.0 + class_3532.method_15350(ShardAimEngine.mc.field_1724.field_6017 * falling, 0.0, 0.7);
        }
        if (!ShardAimEngine.mc.field_1724.method_24828() && ShardAimEngine.mc.field_1724.method_18798().field_1351 > 0.1) {
            strength *= micro ? 0.8 + random.nextDouble(0.15) : 0.85;
        }

        double jitter = micro ? random.nextDouble(-0.002, 0.002) : 0.0;
        double factor = class_3532.method_15350(strength * 0.03 + jitter, 0.004, difference > 30.0 ? 0.62 : 0.48);
        double yawStep = yawDelta * factor;
        double pitchStep = pitchDelta * factor * (micro ? 0.85 + random.nextDouble(0.3) : 1.0);

        if (micro && difference < 2.5 && this.closeBoost > 0.01) {
            double boost = this.closeBoost * random.nextDouble(0.4, 1.0);
            yawStep *= 1.0 + boost;
            pitchStep *= 1.0 + boost * 0.7;
            this.closeBoost *= 0.9 + random.nextDouble(0.07);
        }
        if (micro) {
            double noise = 0.018 + random.nextDouble(0.03);
            this.yawPhase += this.yawPhaseSpeed + random.nextDouble(-0.02, 0.02);
            this.pitchPhase += this.pitchPhaseSpeed + random.nextDouble(-0.02, 0.02);
            yawStep += Math.sin(this.yawPhase) * noise;
            pitchStep += Math.cos(this.pitchPhase) * noise * 0.6;
        }

        // Never snap: cap the absolute step and its change from the previous frame.
        yawStep = class_3532.method_15350(yawStep, -MAX_YAW_STEP, MAX_YAW_STEP);
        pitchStep = class_3532.method_15350(pitchStep, -MAX_PITCH_STEP, MAX_PITCH_STEP);
        double maxYawAcceleration = micro ? 1.1 + random.nextDouble(0.7) : 2.0;
        double maxPitchAcceleration = micro ? 0.9 + random.nextDouble(0.5) : 1.6;
        yawStep = this.limitAcceleration(yawStep, this.previousYawStep, maxYawAcceleration);
        pitchStep = this.limitAcceleration(pitchStep, this.previousPitchStep, maxPitchAcceleration);

        // Shard checks that every rotation delta is a whole mouse-sensitivity step.
        double gcd = MathUtil.computeGcd();
        if (gcd > 1.0E-4) {
            yawStep = this.quantize(yawStep, yawDelta, gcd, difference > gcd);
            pitchStep = this.quantize(pitchStep, pitchDelta, gcd, difference > gcd);
            if (micro) {
                // Keep the micro-movement on the same lattice: 0..2 extra mouse steps, never below the target.
                this.yawPhase += this.yawPhaseSpeed + random.nextDouble(-0.02, 0.02);
                this.pitchPhase += this.pitchPhaseSpeed + random.nextDouble(-0.02, 0.02);
                int extraYaw = (int)Math.round(Math.sin(this.yawPhase) * 0.7 + random.nextDouble(-0.5, 0.5));
                int extraPitch = (int)Math.round(Math.cos(this.pitchPhase) * 0.5 + random.nextDouble(-0.35, 0.35));
                yawStep += (double)extraYaw * gcd;
                pitchStep += (double)extraPitch * gcd;
            }
            if (Math.abs(yawStep) > Math.abs(yawDelta) && Math.abs(yawDelta) >= gcd) {
                yawStep = Math.copySign(Math.floor(Math.abs(yawDelta) / gcd) * gcd, yawDelta);
            }
            if (Math.abs(pitchStep) > Math.abs(pitchDelta) && Math.abs(pitchDelta) >= gcd) {
                pitchStep = Math.copySign(Math.floor(Math.abs(pitchDelta) / gcd) * gcd, pitchDelta);
            }
        }
        // Store exactly what was applied, so the acceleration limiter works on the same numbers the server sees.
        this.previousYawStep = yawStep;
        this.previousPitchStep = pitchStep;
        if (Math.abs(yawStep) < 0.0005 && Math.abs(pitchStep) < 0.0005) {
            return;
        }
        float newYaw = currentYaw + (float)yawStep;
        float newPitch = class_3532.method_15363((float)((double)currentPitch + pitchStep), -90.0f, 90.0f);
        ShardAimEngine.mc.field_1724.method_36456(newYaw);
        ShardAimEngine.mc.field_1724.method_36457(newPitch);
    }

    /**
     * Rounds a step to a whole mouse-sensitivity step. If the step would round to zero while the target is
     * still further than one step away, it moves by exactly one step instead of stalling short.
     */
    private double quantize(double step, double delta, double gcd, boolean force) {
        if (Math.abs(delta) < gcd) {
            return 0.0;   // the target is closer than a single mouse step: do not oscillate around it
        }
        if (Math.abs(step) < gcd * 0.5) {
            if (!force) {
                return 0.0;
            }
            step = Math.copySign(gcd, delta);
        }
        double rounded = Math.round(step / gcd) * gcd;
        if (Math.abs(rounded) > Math.abs(delta)) {
            // never overshoot by more than one step
            rounded = Math.copySign(Math.max(gcd, Math.floor(Math.abs(delta) / gcd) * gcd), delta);
        }
        return rounded;
    }

    private void randomizeProfile() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        this.yawPhase = random.nextDouble(Math.PI * 2);
        this.pitchPhase = random.nextDouble(Math.PI * 2);
        this.yawPhaseSpeed = random.nextDouble(0.05, 0.17);
        this.pitchPhaseSpeed = random.nextDouble(0.04, 0.13);
        this.curvePower = random.nextDouble(1.8, 3.2);
        this.speedScale = random.nextDouble(0.8, 1.2);
        this.closeBoost = random.nextDouble(-0.05, 0.15);
        this.easingMode = random.nextInt(3);
        this.heightOffset = random.nextDouble(-0.1, 0.08);
        this.nextHeightAt = System.currentTimeMillis();
        this.pointLerp = random.nextDouble(0.025, 0.06);
        this.randomizePoint(random);
    }

    private void randomizePoint(ThreadLocalRandom random) {
        this.pointTargetX = random.nextDouble(-0.32, 0.32);
        this.pointTargetY = random.nextDouble(-0.1, 0.1);
        this.pointTargetZ = random.nextDouble(-0.32, 0.32);
        this.nextPointAt = System.currentTimeMillis() + random.nextLong(280L, 850L);
        this.pointLerp = random.nextDouble(0.02, 0.065);
    }

    private double[] calculateAngles(class_1297 entity, double aimHeight, boolean multipoint, boolean micro) {
        class_243 eyes = ShardAimEngine.mc.field_1724.method_33571();
        double height = aimHeight + (micro ? this.heightOffset : 0.0);
        double xOffset = 0.0;
        double yOffset = 0.0;
        double zOffset = 0.0;
        if (multipoint) {
            double halfWidth = (double)entity.method_17681() * 0.5;
            xOffset = this.pointX * halfWidth;
            yOffset = this.pointY;
            zOffset = this.pointZ * halfWidth;
        }
        double heightFactor = class_3532.method_15350(height + yOffset, 0.05, 0.95);
        class_243 point = entity.method_73189().method_1031(xOffset, (double)entity.method_17682() * heightFactor, zOffset);
        class_243 delta = point.method_1020(eyes);
        double horizontal = Math.hypot(delta.field_1352, delta.field_1350);
        double yaw = horizontal < 0.001 ? (double)ShardAimEngine.mc.field_1724.method_36454() : Math.toDegrees(Math.atan2(-delta.field_1352, delta.field_1350));
        double length = delta.method_1033();
        double pitch = length < 0.001 ? (double)ShardAimEngine.mc.field_1724.method_36455() : -Math.toDegrees(Math.asin(class_3532.method_15350(delta.field_1351 / length, -1.0, 1.0)));
        return new double[]{yaw, pitch};
    }

    private double easing(double progress, boolean micro) {
        double clamped = class_3532.method_15350(progress, 0.0, 1.0);
        if (!micro) {
            return 1.0 - Math.pow(1.0 - clamped, 2.5);
        }
        double value = switch (this.easingMode) {
            case 0 -> 1.0 - Math.pow(1.0 - clamped, this.curvePower);
            case 1 -> clamped < 0.5
                    ? Math.pow(2.0 * clamped, this.curvePower) / 2.0
                    : 1.0 - Math.pow(2.0 * (1.0 - clamped), this.curvePower) / 2.0;
            default -> clamped * clamped * (3.0 - 2.0 * clamped);
        };
        return class_3532.method_15350(value * (0.88 + ThreadLocalRandom.current().nextDouble(0.24)), 0.05, 1.6);
    }

    private double limitAcceleration(double value, double previous, double maximum) {
        double delta = value - previous;
        return Math.abs(delta) > maximum ? previous + Math.signum(delta) * maximum : value;
    }

    private double wrapDegrees(double value) {
        if ((value %= 360.0) > 180.0) {
            value -= 360.0;
        }
        if (value < -180.0) {
            value += 360.0;
        }
        return value;
    }
}
