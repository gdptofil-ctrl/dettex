/*
 * "Spacetime" rotation tuned for Space-Times ML/AI aim detection.
 *
 * Profile follows the community-proven MlsAngle/sloth pattern:
 *  - a body-part multipoint is computed once every 60-110 ticks and cached as a
 *    local offset from the target's eye (so it tracks movement instead of going
 *    stale in world space), then interpolated over 10 ticks;
 *  - exponential smoothing towards it with a slowly re-randomized speed;
 *  - only a tiny sinusoidal jitter (<=1 degree), no overshoot and no Gaussian noise;
 *  - GCD/sensitivity quantization is applied by RotationController.
 *
 * PointFinder.offset only resets when the velocity argument is ZERO, so a zero
 * velocity is always passed and randomValue() is zero.
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.shard;

import java.security.SecureRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3545;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.PointFinder;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public class SpacetimeSmoothMode
extends AngleSmoothMode {
    private static final float AIM_DISTANCE = 5.5f;

    private static final float HEAD_MIN = 0.78f;
    private static final float HEAD_MAX = 0.98f;
    private static final float STOMACH_MIN = 0.45f;
    private static final float STOMACH_MAX = 0.72f;
    private static final float ARM_MIN = 0.55f;
    private static final float ARM_MAX = 0.92f;
    private static final float LEG_MIN = 0.10f;
    private static final float LEG_MAX = 0.42f;

    private final SecureRandom random = new SecureRandom();
    private final PointFinder pointFinder = new PointFinder();
    private class_243 cachedLocalPoint;
    private class_243 previousLocalPoint;
    private class_1297 cachedEntity;
    private int tickCounter;
    private int nextUpdateTick;
    private int interpolationTicks;
    private int reactionTicks;
    private int reactionDelay;
    private float attackSpeed;
    private float idleSpeed;
    private float jitterYawAmp;
    private float jitterPitchAmp;
    private float jitterYawPeriod;
    private float jitterPitchPeriod;
    private double jitterPhase;
    private int parameterCounter;
    private int nextParameterUpdate;

    public SpacetimeSmoothMode() {
        super("Spacetime");
        this.nextUpdateTick = this.nextTick();
        this.genParameter();
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 vec, class_1297 entity) {
        Aura aura = Aura.getInstance();
        if (++this.parameterCounter >= this.nextParameterUpdate) {
            this.genParameter();
            this.parameterCounter = 0;
        }
        if (current == null) {
            current = AngleUtil.cameraAngle();
        }
        if (!(entity instanceof class_1309)) {
            this.cachedLocalPoint = null;
            this.previousLocalPoint = null;
            this.cachedEntity = null;
            this.tickCounter = 0;
            return current;
        }
        class_1309 living = (class_1309)entity;
        class_243 reference = living.method_33571();
        if (this.cachedEntity != entity) {
            this.reactionDelay = this.random.nextInt(3);
            this.reactionTicks = 0;
        }
        ++this.tickCounter;
        if (this.cachedLocalPoint == null || this.cachedEntity != entity || this.tickCounter >= this.nextUpdateTick) {
            float[] part = this.pickBodyPart();
            class_3545<class_243, ?> result = this.pointFinder.computeBodyPartVector(living, AIM_DISTANCE, current, false, part[0], part[1], part[2]);
            this.previousLocalPoint = this.cachedLocalPoint;
            this.cachedLocalPoint = ((class_243)result.method_15442()).method_1020(reference);
            this.cachedEntity = entity;
            this.tickCounter = 0;
            this.nextUpdateTick = this.nextTick();
            this.interpolationTicks = 0;
        }
        class_243 localPoint = this.cachedLocalPoint;
        if (this.previousLocalPoint != null && this.interpolationTicks < 10) {
            float lerp = (float)this.interpolationTicks / 10.0f;
            localPoint = this.previousLocalPoint.method_1031(
                (this.cachedLocalPoint.method_10216() - this.previousLocalPoint.method_10216()) * (double)lerp,
                (this.cachedLocalPoint.method_10214() - this.previousLocalPoint.method_10214()) * (double)lerp,
                (this.cachedLocalPoint.method_10215() - this.previousLocalPoint.method_10215()) * (double)lerp);
            ++this.interpolationTicks;
        }
        class_243 targetPoint = reference.method_1019(localPoint);
        target = AngleUtil.fromVec3d(targetPoint.method_1020(SpacetimeSmoothMode.mc.field_1724.method_33571()));

        Angle angleDelta = AngleUtil.calculateDelta(current, target);
        boolean hasTarget = aura != null && aura.isState() && aura.getTarget() != null;
        float speed;
        if (this.reactionTicks < this.reactionDelay) {
            ++this.reactionTicks;
            speed = this.idleSpeed * 0.4f;
        } else {
            speed = hasTarget ? this.attackSpeed : this.idleSpeed;
        }
        this.jitterPhase += 1.0;
        float jitterYaw = (float)((double)this.jitterYawAmp * Math.sin(this.jitterPhase / (double)this.jitterYawPeriod));
        float jitterPitch = (float)((double)this.jitterPitchAmp * Math.sin(this.jitterPhase / (double)this.jitterPitchPeriod));
        float stepYaw = class_3532.method_15363(angleDelta.getYaw() * speed + jitterYaw, -90.0f, 90.0f);
        float stepPitch = class_3532.method_15363(angleDelta.getPitch() * speed + jitterPitch, -60.0f, 60.0f);
        float newYaw = current.getYaw() + stepYaw;
        float newPitch = class_3532.method_15363(current.getPitch() + stepPitch, -90.0f, 90.0f);
        return new Angle(newYaw, newPitch);
    }

    private float[] pickBodyPart() {
        int roll = this.random.nextInt(100);
        if (roll < 30) {
            return new float[]{HEAD_MIN, HEAD_MAX, 0.0f};
        }
        if (roll < 60) {
            return new float[]{STOMACH_MIN, STOMACH_MAX, 0.0f};
        }
        if (roll < 82) {
            float lateral = this.random.nextBoolean() ? 1.0f : -1.0f;
            return new float[]{ARM_MIN, ARM_MAX, lateral};
        }
        return new float[]{LEG_MIN, LEG_MAX, 0.0f};
    }

    private void genParameter() {
        this.attackSpeed = 0.55f + this.random.nextFloat() * 0.15f;
        this.idleSpeed = 0.22f + this.random.nextFloat() * 0.16f;
        this.jitterYawAmp = 0.25f + this.random.nextFloat() * 0.65f;
        this.jitterPitchAmp = 0.15f + this.random.nextFloat() * 0.45f;
        this.jitterYawPeriod = 22.0f + this.random.nextFloat() * 23.0f;
        this.jitterPitchPeriod = 26.0f + this.random.nextFloat() * 23.0f;
        this.nextParameterUpdate = 20 + this.random.nextInt(30);
    }

    private int nextTick() {
        return 60 + this.random.nextInt(50);
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}
