/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.aim;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.math.MathUtil;

public final class LegitAimAssistEngine
implements QuickImports {
    private class_1297 target;
    private long acquiredAt;
    private long lastFrameNanos;
    private double previousYawStep;
    private double previousPitchStep;
    private double yawPhase;
    private double pitchPhase;
    private double yawPhaseSpeed;
    private double pitchPhaseSpeed;
    private double curvePower;
    private double speedScale;
    private double closeBoost;
    private double heightOffset;
    private long heightUpdatedAt;
    private int easingMode;
    private double pointTargetX;
    private double pointTargetY;
    private double pointTargetZ;
    private double pointX;
    private double pointY;
    private double pointZ;
    private double pointLerp;
    private long nextPointAt;

    public void reset() {
        this.target = null;
        this.acquiredAt = 0L;
        this.lastFrameNanos = 0L;
        this.previousYawStep = 0.0;
        this.previousPitchStep = 0.0;
        this.pointX = 0.0;
        this.pointY = 0.0;
        this.pointZ = 0.0;
        this.nextPointAt = 0L;
        this.randomizeProfile();
    }

    public void update(class_1297 entity, double speed, double aimHeight, boolean onlyYaw, boolean multipoint, boolean microMovements) {
        double difference;
        double pitchDelta;
        if (LegitAimAssistEngine.mc.field_1724 == null || entity == null) {
            if (this.target != null) {
                this.reset();
            }
            return;
        }
        long frameNanos = System.nanoTime();
        double frameScale = this.lastFrameNanos > 0L ? (double)(frameNanos - this.lastFrameNanos) / 1.66666667E7 : 1.0;
        frameScale = class_3532.method_15350((double)frameScale, (double)0.05, (double)3.0);
        this.lastFrameNanos = frameNanos;
        long now = System.currentTimeMillis();
        if (entity != this.target) {
            this.target = entity;
            this.acquiredAt = now;
            this.randomizeProfile();
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (microMovements && now - this.heightUpdatedAt > random.nextLong(180L, 450L)) {
            this.heightOffset = class_3532.method_15350((double)(this.heightOffset + random.nextDouble(-0.06, 0.06)), (double)-0.2, (double)0.15);
            this.heightUpdatedAt = now;
        }
        if (multipoint && now >= this.nextPointAt) {
            this.randomizePoint(random);
        }
        if (multipoint) {
            double lerp = this.pointLerp + (microMovements ? random.nextDouble(-0.008, 0.008) : 0.0);
            this.pointX += (this.pointTargetX - this.pointX) * lerp;
            this.pointY += (this.pointTargetY - this.pointY) * lerp;
            this.pointZ += (this.pointTargetZ - this.pointZ) * lerp;
        }
        double[] desired = this.calculateAngles(entity, aimHeight, multipoint, microMovements);
        float currentYaw = LegitAimAssistEngine.mc.field_1724.method_36454();
        float currentPitch = LegitAimAssistEngine.mc.field_1724.method_36455();
        double yawDelta = this.wrapDegrees(desired[0] - (double)currentYaw);
        double rawPitchDelta = desired[1] - (double)currentPitch;
        if (onlyYaw) {
            double deadZone = microMovements ? 8.0 + random.nextDouble(4.0) : 10.0;
            double absolutePitch = Math.abs(rawPitchDelta);
            if (absolutePitch > deadZone) {
                double pull = microMovements ? 0.06 + random.nextDouble(0.06) : 0.08;
                pitchDelta = (rawPitchDelta - Math.signum(rawPitchDelta) * deadZone) * pull;
            } else {
                pitchDelta = 0.0;
            }
        } else {
            pitchDelta = rawPitchDelta;
        }
        double d = difference = onlyYaw ? Math.abs(yawDelta) : Math.hypot(yawDelta, pitchDelta);
        if (difference < 0.05) {
            return;
        }
        double acquireDuration = microMovements ? 400.0 + random.nextDouble(200.0) : 350.0;
        double acquireProgress = class_3532.method_15350((double)((double)(now - this.acquiredAt) / acquireDuration), (double)0.0, (double)1.0);
        double curve = this.easing(acquireProgress, microMovements);
        double baseSpeed = speed * (microMovements ? this.speedScale : 1.0);
        if (microMovements && random.nextDouble() < 0.015) {
            this.speedScale = random.nextDouble(0.7, 1.3);
        }
        double differenceScale = microMovements ? (difference < 3.0 ? 0.35 + random.nextDouble(0.25) : (difference < 10.0 ? 0.7 + random.nextDouble(0.3) : (difference < 25.0 ? 0.85 + random.nextDouble(0.3) : (difference < 50.0 ? 1.15 + random.nextDouble(0.35) : 1.4 + random.nextDouble(0.4))))) : (difference < 3.0 ? 0.45 : (difference < 10.0 ? 0.85 : (difference < 25.0 ? 1.0 : (difference < 50.0 ? 1.3 : 1.6))));
        double largeDifferenceScale = 1.0;
        if (difference > 15.0) {
            double progress = class_3532.method_15350((double)((difference - 15.0) / 60.0), (double)0.0, (double)1.0);
            largeDifferenceScale += progress * (microMovements ? 1.2 + random.nextDouble(0.4) : 1.4);
        }
        double strength = baseSpeed * curve * differenceScale * largeDifferenceScale * frameScale;
        if (LegitAimAssistEngine.mc.field_1724.field_6017 > 0.0) {
            double fallingFactor = microMovements ? 0.12 + random.nextDouble(0.08) : 0.15;
            strength *= 1.0 + class_3532.method_15350((double)(LegitAimAssistEngine.mc.field_1724.field_6017 * fallingFactor), (double)0.0, (double)0.7);
        }
        if (!LegitAimAssistEngine.mc.field_1724.method_24828() && LegitAimAssistEngine.mc.field_1724.method_18798().field_1351 > 0.1) {
            strength *= microMovements ? 0.8 + random.nextDouble(0.15) : 0.85;
        }
        double jitter = microMovements ? random.nextDouble(-0.003, 0.003) : 0.0;
        double factor = class_3532.method_15350((double)(strength * 0.028 + jitter), (double)0.005, (double)(difference > 30.0 ? 0.96 : 0.92));
        double yawStep = yawDelta * factor;
        double pitchStep = pitchDelta * factor * (microMovements ? 0.85 + random.nextDouble(0.3) : 1.0);
        if (microMovements && difference < 2.5 && this.closeBoost > 0.01) {
            double boost = this.closeBoost * random.nextDouble(0.4, 1.0);
            yawStep *= 1.0 + boost;
            pitchStep *= 1.0 + boost * 0.7;
            this.closeBoost *= 0.9 + random.nextDouble(0.07);
        }
        if (microMovements) {
            double noise = 0.06 + random.nextDouble(0.1);
            this.yawPhase += this.yawPhaseSpeed + random.nextDouble(-0.03, 0.03);
            this.pitchPhase += this.pitchPhaseSpeed + random.nextDouble(-0.025, 0.025);
            yawStep += Math.sin(this.yawPhase) * noise * (0.5 + random.nextDouble(0.8));
            pitchStep += Math.cos(this.pitchPhase) * noise * 0.55 * (0.4 + random.nextDouble(0.7));
        }
        double maxYawAcceleration = microMovements ? 2.0 + random.nextDouble(1.5) : 3.0;
        double maxPitchAcceleration = microMovements ? 1.5 + random.nextDouble(1.0) : 2.5;
        yawStep = this.limitAcceleration(yawStep, this.previousYawStep, maxYawAcceleration);
        pitchStep = this.limitAcceleration(pitchStep, this.previousPitchStep, maxPitchAcceleration);
        this.previousYawStep = yawStep;
        this.previousPitchStep = pitchStep;
        if (microMovements) {
            double gcd = MathUtil.computeGcd();
            yawStep = this.quantize(yawStep, gcd);
            pitchStep = this.quantize(pitchStep, gcd);
        }
        if (Math.abs(yawStep) >= 0.003 || Math.abs(pitchStep) >= 0.003) {
            LegitAimAssistEngine.mc.field_1724.method_36456(currentYaw + (float)yawStep);
            LegitAimAssistEngine.mc.field_1724.method_36457(class_3532.method_15363((float)(currentPitch + (float)pitchStep), (float)-90.0f, (float)90.0f));
        }
    }

    private void randomizeProfile() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        this.yawPhase = random.nextDouble(Math.PI * 2);
        this.pitchPhase = random.nextDouble(Math.PI * 2);
        this.yawPhaseSpeed = random.nextDouble(0.08, 0.25);
        this.pitchPhaseSpeed = random.nextDouble(0.06, 0.2);
        this.curvePower = random.nextDouble(1.8, 3.5);
        this.speedScale = random.nextDouble(0.75, 1.25);
        this.closeBoost = random.nextDouble(-0.1, 0.2);
        this.easingMode = random.nextInt(3);
        this.heightOffset = random.nextDouble(-0.15, 0.1);
        this.heightUpdatedAt = System.currentTimeMillis();
        this.pointLerp = random.nextDouble(0.025, 0.065);
        this.randomizePoint(random);
    }

    private void randomizePoint(ThreadLocalRandom random) {
        this.pointTargetX = random.nextDouble(-0.35, 0.35);
        this.pointTargetY = random.nextDouble(-0.12, 0.12);
        this.pointTargetZ = random.nextDouble(-0.35, 0.35);
        this.nextPointAt = System.currentTimeMillis() + random.nextLong(300L, 900L);
        this.pointLerp = random.nextDouble(0.02, 0.07);
    }

    private double[] calculateAngles(class_1297 entity, double aimHeight, boolean multipoint, boolean microMovements) {
        class_243 eyes = LegitAimAssistEngine.mc.field_1724.method_33571();
        double height = aimHeight + (microMovements ? this.heightOffset : 0.0);
        double xOffset = 0.0;
        double yOffset = 0.0;
        double zOffset = 0.0;
        if (multipoint) {
            double halfWidth = (double)entity.method_17681() * 0.5;
            xOffset = this.pointX * halfWidth;
            yOffset = this.pointY;
            zOffset = this.pointZ * halfWidth;
        }
        double heightFactor = class_3532.method_15350((double)(height + yOffset), (double)0.05, (double)0.95);
        class_243 point = entity.method_73189().method_1031(xOffset, (double)entity.method_17682() * heightFactor, zOffset);
        class_243 delta = point.method_1020(eyes);
        double horizontal = Math.hypot(delta.field_1352, delta.field_1350);
        double yaw = horizontal < 0.001 ? (double)LegitAimAssistEngine.mc.field_1724.method_36454() : Math.toDegrees(Math.atan2(-delta.field_1352, delta.field_1350));
        double length = delta.method_1033();
        double pitch = length < 0.001 ? (double)LegitAimAssistEngine.mc.field_1724.method_36455() : -Math.toDegrees(Math.asin(class_3532.method_15350((double)(delta.field_1351 / length), (double)-1.0, (double)1.0)));
        return new double[]{yaw, pitch};
    }

    private double easing(double progress, boolean microMovements) {
        double clamped = class_3532.method_15350((double)progress, (double)0.0, (double)1.0);
        if (!microMovements) {
            return 1.0 - Math.pow(1.0 - clamped, 2.5);
        }
        double value = switch (this.easingMode) {
            case 0 -> 1.0 - Math.pow(1.0 - clamped, this.curvePower);
            case 1 -> {
                if (clamped < 0.5) {
                    yield Math.pow(2.0 * clamped, this.curvePower) / 2.0;
                }
                yield 1.0 - Math.pow(2.0 * (1.0 - clamped), this.curvePower) / 2.0;
            }
            default -> clamped * clamped * (3.0 - 2.0 * clamped);
        };
        return class_3532.method_15350((double)(value * (0.85 + ThreadLocalRandom.current().nextDouble(0.3))), (double)0.05, (double)1.8);
    }

    private double limitAcceleration(double value, double previous, double maximum) {
        double delta = value - previous;
        return Math.abs(delta) > maximum ? previous + Math.signum(delta) * maximum : value;
    }

    private double quantize(double value, double gcd) {
        return gcd <= 1.0E-4 ? value : value - value % gcd;
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

