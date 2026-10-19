/*
 * "Shard 2.1" rotation, tuned against KaelusAI's Shard 2.x ML aim check.
 *
 * What Shard 2.1 actually measures (v2.1.0 sources):
 *  - AimProcessor estimates the mouse sensitivity step (gcd) per axis from the
 *    running mode of gcd(consecutive rotation deltas) -> mode_yaw / mode_pitch.
 *  - It then derives delta_dots = |delta| / mode ("mouse steps" this tick).
 *  - TickData additionally streams aim_error, aim_error_yaw/pitch,
 *    hit_point_rel_*, raytrace_hit, raytrace_dirs_hit_count, ticks_to_attack,
 *    attack_cooldown_progress, exposure, ... into the model.
 *  The model therefore separates humans from automation by the *shape* of the
 *  mouse movement (delta_dots sequence, hit point spread, aim error), not by a
 *  single constant.
 *
 * Design:
 *  - The aim point is a cached body-part multipoint (stored relative to the
 *    target eye and interpolated over 10 ticks) so the hit point wanders like a
 *    human instead of snapping to one spot.
 *  - Movement is produced in integer "mouse dots": the requested angular delta is
 *    converted to a dot count using stochastic rounding, so every delta that
 *    leaves the client is an exact multiple of the vanilla sensitivity step
 *    (keeps mode_yaw/mode_pitch valid and realistic) while the per-tick dot count
 *    still varies naturally.
 *  - Speed is a mean-reverting random walk re-seeded periodically, with a
 *    reaction delay on target switch, an idle slow-down, occasional +/-1 dot
 *    micro-corrections and small gaussian motor noise.
 *  - No sub-step jitter is added, so gcd(consecutive deltas) never collapses to
 *    float noise and the running mode stays valid for both axes.
 *
 * GCD is applied here (this mode is exempt from RotationController's central
 * adjustSensitivity), exactly like SpookyTime / ReallyWorld.
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.shard;

import java.security.SecureRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3545;
import ru.recoin.common.util.crystal.AimQuantizer;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.PointFinder;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public class Shard21SmoothMode
extends AngleSmoothMode {
    private static final float AIM_DISTANCE = 5.5f;

    private static final float HEAD_MIN = 0.80f;
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

    private float yawSpeed;
    private float pitchSpeed;
    private float yawSpeedTarget;
    private float pitchSpeedTarget;

    private int parameterCounter;
    private int nextParameterUpdate;

    public Shard21SmoothMode() {
        super("Shard 2.1");
        this.nextUpdateTick = this.nextTick();
        this.genParameter();
        this.yawSpeed = this.yawSpeedTarget;
        this.pitchSpeed = this.pitchSpeedTarget;
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 vec, class_1297 entity) {
        Aura aura = Aura.getInstance();
        if (current == null) {
            current = AngleUtil.cameraAngle();
        }
        if (++this.parameterCounter >= this.nextParameterUpdate) {
            this.genParameter();
            this.parameterCounter = 0;
        }
        this.tickSpeed();

        if (entity instanceof class_1309) {
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
            target = AngleUtil.fromVec3d(targetPoint.method_1020(Shard21SmoothMode.mc.field_1724.method_33571()));
        } else {
            this.resetCache();
            if (target == null) {
                return current;
            }
        }

        Angle delta = AngleUtil.calculateDelta(current, target);

        double gcd = Math.max(1.0E-4, (double)AimQuantizer.getStep());

        boolean hasTarget = aura != null && aura.isState() && aura.getTarget() != null;

        double speedYaw = (double)this.yawSpeed * (1.0 + this.random.nextGaussian() * 0.07);
        double speedPitch = (double)this.pitchSpeed * (1.0 + this.random.nextGaussian() * 0.07);
        if (this.reactionTicks < this.reactionDelay) {
            ++this.reactionTicks;
            speedYaw *= 0.35;
            speedPitch *= 0.35;
        }
        if (!hasTarget) {
            speedYaw *= 0.55;
            speedPitch *= 0.5;
        }
        speedYaw = clamp(speedYaw, 0.05, 0.95);
        speedPitch = clamp(speedPitch, 0.05, 0.95);

        double yawDotsDesired = (double)delta.getYaw() * speedYaw / gcd + this.random.nextGaussian() * 0.55;
        double pitchDotsDesired = (double)delta.getPitch() * speedPitch / gcd + this.random.nextGaussian() * 0.35;

        long yawDots = stochasticRound(yawDotsDesired);
        long pitchDots = stochasticRound(pitchDotsDesired);

        if (hasTarget && Math.abs(delta.getYaw()) > gcd * 2.0 && this.random.nextFloat() < 0.05f) {
            yawDots += this.random.nextBoolean() ? 1L : -1L;
        }

        float newYaw = (float)((double)current.getYaw() + (double)yawDots * gcd);
        float newPitch = (float)((double)current.getPitch() + (double)pitchDots * gcd);
        newPitch = class_3532.method_15363(newPitch, -90.0f, 90.0f);
        return new Angle(newYaw, newPitch);
    }

    private void tickSpeed() {
        this.yawSpeed += (this.yawSpeedTarget - this.yawSpeed) * 0.15f;
        this.pitchSpeed += (this.pitchSpeedTarget - this.pitchSpeed) * 0.15f;
    }

    private float[] pickBodyPart() {
        int roll = this.random.nextInt(100);
        if (roll < 32) {
            return new float[]{HEAD_MIN, HEAD_MAX, 0.0f};
        }
        if (roll < 62) {
            return new float[]{STOMACH_MIN, STOMACH_MAX, 0.0f};
        }
        if (roll < 84) {
            float lateral = this.random.nextBoolean() ? 1.0f : -1.0f;
            return new float[]{ARM_MIN, ARM_MAX, lateral};
        }
        return new float[]{LEG_MIN, LEG_MAX, 0.0f};
    }

    private void genParameter() {
        this.yawSpeedTarget = 0.45f + this.random.nextFloat() * 0.25f;
        this.pitchSpeedTarget = 0.30f + this.random.nextFloat() * 0.22f;
        this.nextParameterUpdate = 18 + this.random.nextInt(28);
    }

    private int nextTick() {
        return 60 + this.random.nextInt(50);
    }

    private void resetCache() {
        this.cachedLocalPoint = null;
        this.previousLocalPoint = null;
        this.cachedEntity = null;
        this.tickCounter = 0;
        this.interpolationTicks = 0;
    }

    private long stochasticRound(double value) {
        long floor = (long)Math.floor(value);
        double fraction = value - (double)floor;
        return fraction > 0.0 && this.random.nextDouble() < fraction ? floor + 1L : floor;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}
