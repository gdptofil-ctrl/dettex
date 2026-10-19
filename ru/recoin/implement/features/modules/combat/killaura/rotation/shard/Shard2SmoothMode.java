/*
 * Sloth/Shard bypass rotation ("Shard-2"). Structure inspired by the community
 * "MlsAngle" rotation: a multi-point is computed once every 70-131 ticks and
 * cached as a local offset from the target, then interpolated; speeds/jitter are
 * randomized periodically. Imports/API adapted to Recoin.
 *
 * Important Recoin notes:
 *  - PointFinder.offset only resets when the velocity argument is ZERO, otherwise
 *    it accumulates every call and drags the aim point off the target. We always
 *    pass a zero velocity (and expose a zero randomValue()) so the offset stays 0.
 *  - The cached point is stored RELATIVE to the target's eye so it tracks the
 *    target's movement instead of going stale in world space.
 *  - Jitter is kept tiny (<=1 degree) so the crosshair does not visibly shake and
 *    the aim stays on the hitbox.
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

public class Shard2SmoothMode
extends AngleSmoothMode {
    private final SecureRandom random = new SecureRandom();
    private final PointFinder pointFinder = new PointFinder();
    private class_243 cachedLocalPoint;
    private class_243 previousLocalPoint;
    private class_1297 cachedEntity;
    private int tickCounter;
    private int nextUpdateTick;
    private int interpolationTicks;
    private float attackSpeed;
    private float idleSpeed;
    private float jitterYawAmp;
    private float jitterPitchAmp;
    private float jitterYawPeriod;
    private float jitterPitchPeriod;
    private double jitterPhase;
    private int parameterCounter;
    private int nextParameterUpdate;

    public Shard2SmoothMode() {
        super("Shard-2");
        this.nextUpdateTick = this.nextTick();
        this.genParameter();
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, class_243 vec3d, class_1297 entity) {
        Aura aura = Aura.getInstance();
        if (++this.parameterCounter >= this.nextParameterUpdate) {
            this.genParameter();
            this.parameterCounter = 0;
        }
        if (entity instanceof class_1309) {
            class_1309 livingEntity = (class_1309)entity;
            class_243 referencePos = livingEntity.method_33571();
            ++this.tickCounter;
            if (this.cachedLocalPoint == null || this.cachedEntity != entity || this.tickCounter >= this.nextUpdateTick) {
                class_3545<class_243, ?> multiPointResult = this.pointFinder.computeMultiPointVector(livingEntity, 6.0f, currentAngle, class_243.field_1353, false);
                this.previousLocalPoint = this.cachedLocalPoint;
                this.cachedLocalPoint = ((class_243)multiPointResult.method_15442()).method_1020(referencePos);
                this.cachedEntity = entity;
                this.tickCounter = 0;
                this.nextUpdateTick = this.nextTick();
                this.interpolationTicks = 0;
            }
            class_243 localPoint = this.cachedLocalPoint;
            if (this.previousLocalPoint != null && this.interpolationTicks < 10) {
                float lerpFactor = (float)this.interpolationTicks / 10.0f;
                localPoint = this.previousLocalPoint.method_1031(
                    (this.cachedLocalPoint.method_10216() - this.previousLocalPoint.method_10216()) * (double)lerpFactor,
                    (this.cachedLocalPoint.method_10214() - this.previousLocalPoint.method_10214()) * (double)lerpFactor,
                    (this.cachedLocalPoint.method_10215() - this.previousLocalPoint.method_10215()) * (double)lerpFactor);
                ++this.interpolationTicks;
            }
            class_243 targetPoint = referencePos.method_1019(localPoint);
            targetAngle = AngleUtil.fromVec3d(targetPoint.method_1020(Shard2SmoothMode.mc.field_1724.method_33571()));
        } else {
            this.cachedLocalPoint = null;
            this.previousLocalPoint = null;
            this.cachedEntity = null;
            this.tickCounter = 0;
        }
        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        boolean hasTarget = entity != null && aura.isState() && aura.getTarget() != null;
        float speed = hasTarget ? this.attackSpeed : this.idleSpeed;
        this.jitterPhase += 1.0;
        float jitterYaw = (float)((double)this.jitterYawAmp * Math.sin(this.jitterPhase / (double)this.jitterYawPeriod));
        float jitterPitch = (float)((double)this.jitterPitchAmp * Math.sin(this.jitterPhase / (double)this.jitterPitchPeriod));
        float newYaw = currentAngle.getYaw() + angleDelta.getYaw() * speed + jitterYaw;
        float newPitch = class_3532.method_15363(currentAngle.getPitch() + angleDelta.getPitch() * speed + jitterPitch, -90.0f, 90.0f);
        return new Angle(newYaw, newPitch);
    }

    private void genParameter() {
        this.attackSpeed = 0.5f + this.random.nextFloat() * 0.2f;
        this.idleSpeed = 0.22f + this.random.nextFloat() * 0.16f;
        this.jitterYawAmp = 0.3f + this.random.nextFloat() * 0.7f;
        this.jitterPitchAmp = 0.2f + this.random.nextFloat() * 0.5f;
        this.jitterYawPeriod = 20.0f + this.random.nextFloat() * 25.0f;
        this.jitterPitchPeriod = 24.0f + this.random.nextFloat() * 25.0f;
        this.nextParameterUpdate = 20 + this.random.nextInt(30);
    }

    private int nextTick() {
        return 70 + this.random.nextInt(62);
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}
