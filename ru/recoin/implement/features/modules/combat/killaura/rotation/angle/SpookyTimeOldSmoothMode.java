/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.target.BacktrackPositionResolver;

public final class SpookyTimeOldSmoothMode
extends AngleSmoothMode {
    private final float[] speedHistory = new float[5];
    private class_1297 trackedTarget;
    private long lostAimAt = -1L;
    private int speedHistoryIndex;
    private long lastHistoryTick;
    private Angle lastAttackAngle;

    public SpookyTimeOldSmoothMode() {
        super("SpookyTime Old");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 point, class_1297 entity) {
        if (entity == null) {
            this.trackedTarget = null;
            this.lostAimAt = -1L;
            return target != null ? target : current;
        }
        return current;
    }

    public Angle updateFrame(Angle current, class_1297 target, float attackRange, float followRange) {
        if (SpookyTimeOldSmoothMode.mc.field_1724 == null || target == null) {
            return current;
        }
        if (target != this.trackedTarget) {
            this.trackedTarget = target;
            this.lostAimAt = -1L;
        }
        Angle from = current != null ? current : AngleUtil.cameraAngle();
        class_243 eyes = SpookyTimeOldSmoothMode.mc.field_1724.method_33571();
        class_243 speedPointDirection = BacktrackPositionResolver.position(target).method_1031(0.0, (double)target.method_17682() * 0.55, 0.0).method_1020(eyes).method_1029();
        float speedTargetYaw = (float)Math.toDegrees(Math.atan2(-speedPointDirection.field_1352, speedPointDirection.field_1350));
        long now = System.currentTimeMillis();
        float yawDelta = class_3532.method_15393((float)(speedTargetYaw - from.getYaw()));
        double distance = SpookyTimeOldSmoothMode.mc.field_1724.method_5739(target);
        float random = (float)Math.random();
        float absoluteYaw = Math.abs(yawDelta);
        float factor = distance < 2.5 ? (absoluteYaw < 8.0f ? 0.36f + random * 0.08f : 0.5f + random * 0.1f) : (distance > 5.5 ? (absoluteYaw < 8.0f ? 0.13f + random * 0.05f : 0.18f + random * 0.06f) : (absoluteYaw < 8.0f ? 0.2f + random * 0.06f : 0.3f + random * 0.08f));
        float targetReach = attackRange + followRange + 0.1f;
        boolean primaryRay = this.rayIntersects(from, attackRange + targetReach, BacktrackPositionResolver.boundingBox(target));
        boolean secondaryRay = this.rayIntersects(from, targetReach, BacktrackPositionResolver.boundingBox(target));
        if (primaryRay) {
            factor *= secondaryRay ? 0.7f : 0.92f;
            this.lostAimAt = -1L;
        } else {
            if (this.lostAimAt == -1L) {
                this.lostAimAt = now;
            }
            factor *= Math.min(1.0f + (float)(now - this.lostAimAt) / 380.0f, 1.9f);
        }
        double yawSpeed = (double)(absoluteYaw * factor) + (Math.random() - 0.5) * 0.05;
        double d = primaryRay ? class_3532.method_15350((double)yawSpeed, (double)0.15, (double)(distance < 3.0 ? 6.0 : 3.5)) : (yawSpeed = class_3532.method_15350((double)yawSpeed, (double)1.2, (double)14.0));
        if (now != this.lastHistoryTick) {
            this.speedHistory[this.speedHistoryIndex] = (float)Math.abs(yawSpeed);
            this.speedHistoryIndex = (this.speedHistoryIndex + 1) % this.speedHistory.length;
            this.lastHistoryTick = now;
        }
        float historySum = 0.0f;
        for (float speed : this.speedHistory) {
            historySum += speed;
        }
        if (historySum > 22.0f && primaryRay) {
            float historyScale = 22.0f / historySum;
            int latest = (this.speedHistoryIndex - 1 + this.speedHistory.length) % this.speedHistory.length;
            this.speedHistory[latest] = (float)Math.abs(yawSpeed *= (double)historyScale);
        }
        double pitchSpeed = primaryRay ? (secondaryRay ? (Math.random() - 0.5) * 0.025 : 0.2 + Math.random() * 0.45) : 3.5 + Math.random() * 2.0;
        yawSpeed += (Math.random() - 0.5) * 0.06;
        yawSpeed = Math.max(yawSpeed, 0.07);
        double ignoredYawReturnSpeed = 8.0 + Math.random() * 2.0;
        double ignoredPitchReturnSpeed = 3.0 + Math.random() * 2.0;
        Angle requested = this.requestedRotation(from, target, eyes);
        return this.applyRotation(from, requested, (float)yawSpeed, (float)pitchSpeed);
    }

    private Angle requestedRotation(Angle current, class_1297 target, class_243 eyes) {
        double maximumHeight = (double)target.method_17682() * 0.55;
        class_243 position = BacktrackPositionResolver.position(target);
        double height = class_3532.method_15350((double)(eyes.field_1351 - position.field_1351), (double)0.0, (double)maximumHeight);
        class_243 direction = position.method_1031(0.0, height, 0.0).method_1020(eyes).method_1029();
        float targetYaw = (float)Math.toDegrees(Math.atan2(-direction.field_1352, direction.field_1350));
        float targetPitch = (float)class_3532.method_15350((double)(-Math.toDegrees(Math.atan2(direction.field_1351, Math.hypot(direction.field_1352, direction.field_1350)))), (double)-90.0, (double)90.0);
        targetPitch = current.getPitch() + (targetPitch - current.getPitch()) * 0.72f;
        return new Angle(current.getYaw() + (float)Math.ceil(class_3532.method_15393((float)targetYaw) - class_3532.method_15393((float)current.getYaw())), current.getPitch() + (float)Math.ceil(class_3532.method_15393((float)targetPitch) - class_3532.method_15393((float)current.getPitch())));
    }

    private Angle applyRotation(Angle current, Angle requested, float yawSpeed, float pitchSpeed) {
        float yawDelta = class_3532.method_15393((float)(requested.getYaw() - current.getYaw()));
        float pitchDelta = requested.getPitch() - current.getPitch();
        float yawLimit = Math.min(Math.abs(yawDelta), yawSpeed);
        float pitchLimit = Math.min(Math.abs(pitchDelta), pitchSpeed);
        float yawMove = this.snapToStep(class_3532.method_15363((float)yawDelta, (float)(-yawLimit), (float)yawLimit));
        float pitchMove = this.snapToStep(class_3532.method_15363((float)pitchDelta, (float)(-pitchLimit), (float)pitchLimit));
        float step = this.rotationStep();
        float yaw = current.getYaw() + (yawMove - yawMove % step);
        float pitchCandidate = class_3532.method_15363((float)(current.getPitch() + pitchMove), (float)-90.0f, (float)90.0f);
        float pitch = pitchCandidate - pitchCandidate % step;
        return new Angle(yaw, pitch);
    }

    private boolean rayIntersects(Angle angle, double range, class_238 box) {
        class_243 eyes = SpookyTimeOldSmoothMode.mc.field_1724.method_33571();
        return box.method_1006(eyes) || box.method_992(eyes, eyes.method_1019(angle.toVector().method_1021(range))).isPresent();
    }

    private float snapToStep(float value) {
        float step = this.rotationStep();
        return (float)Math.round(value / step) * step;
    }

    private float rotationStep() {
        double sensitivity = (Double)SpookyTimeOldSmoothMode.mc.field_1690.method_42495().method_41753();
        double factor = Math.pow(sensitivity * 0.6 + 0.2, 3.0) * 8.0;
        return (float)(factor * 0.015);
    }

    public void rememberAttack(Angle attackAngle) {
        if (attackAngle != null) {
            this.lastAttackAngle = new Angle(attackAngle.getYaw(), class_3532.method_15363((float)attackAngle.getPitch(), (float)-90.0f, (float)90.0f));
        }
    }

    public Angle consumeLastAttackAngle() {
        Angle result = this.lastAttackAngle;
        this.lastAttackAngle = null;
        return result;
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}

