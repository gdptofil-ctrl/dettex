/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.recoin.implement.features.modules.combat.killaura.target.BacktrackPositionResolver;

public final class SpookyTimeSmoothMode
extends AngleSmoothMode {
    private float currentSpeedYaw = 24.0f;
    private float currentSpeedPitch = 6.0f;
    private int attackPauseTicks;
    private Angle attackPauseAngle;

    public SpookyTimeSmoothMode() {
        super("SpookyTime");
    }

    public void onAttack(Angle current) {
        this.attackPauseAngle = current == null ? AngleUtil.cameraAngle() : new Angle(current.getYaw(), current.getPitch());
        this.attackPauseTicks = 3;
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 point, class_1297 entity) {
        float rawPitch;
        float rawYaw;
        Angle from;
        Angle angle = from = current != null ? current : AngleUtil.cameraAngle();
        if (SpookyTimeSmoothMode.mc.field_1724 == null) {
            return from;
        }
        if (this.attackPauseTicks > 0) {
            --this.attackPauseTicks;
            return this.attackPauseAngle;
        }
        if (target == null && entity == null) {
            return current;
        }
        if (entity instanceof class_1309) {
            class_1309 livingTarget = (class_1309)entity;
            double yExpand = class_3532.method_15350((double)(livingTarget.method_23320() - livingTarget.method_23318() - 0.3), (double)0.0, (double)livingTarget.method_17682());
            class_243 vec = BacktrackPositionResolver.position((class_1297)livingTarget).method_1031(0.0, yExpand, 0.0).method_1020(SpookyTimeSmoothMode.mc.field_1724.method_33571());
            rawYaw = (float)Math.toDegrees(Math.atan2(-vec.field_1352, vec.field_1350));
            rawPitch = (float)class_3532.method_15350((double)(-Math.toDegrees(Math.atan2(vec.field_1351, Math.hypot(vec.field_1352, vec.field_1350)))), (double)-90.0, (double)90.0);
        } else if (target != null) {
            rawYaw = target.getYaw();
            rawPitch = target.getPitch();
        } else {
            return from;
        }
        float targetSpeedYaw = SpookyTimeSmoothMode.randomLerp(45.0f, 60.0f);
        float targetSpeedPitch = SpookyTimeSmoothMode.randomLerp(8.0f, 14.0f);
        float smoothFactor = 0.35f;
        if (entity != null && this.rayIntersects(from, 10.0, entity)) {
            targetSpeedYaw = 0.0f;
            targetSpeedPitch = 0.0f;
        }
        this.currentSpeedYaw += (targetSpeedYaw - this.currentSpeedYaw) * smoothFactor;
        this.currentSpeedPitch += (targetSpeedPitch - this.currentSpeedPitch) * smoothFactor;
        float yawDelta = class_3532.method_15393((float)(rawYaw - from.getYaw()));
        float pitchDelta = rawPitch - from.getPitch();
        float clampedYaw = Math.min(Math.abs(yawDelta), this.currentSpeedYaw);
        float clampedPitch = Math.min(Math.abs(pitchDelta), this.currentSpeedPitch);
        float yawMove = class_3532.method_15363((float)yawDelta, (float)(-clampedYaw), (float)clampedYaw);
        float pitchMove = class_3532.method_15363((float)pitchDelta, (float)(-clampedPitch), (float)clampedPitch);
        Angle result = new Angle(from.getYaw() + yawMove, class_3532.method_15363((float)(from.getPitch() + pitchMove), (float)-90.0f, (float)90.0f));
        return result.adjustSensitivity();
    }

    private boolean rayIntersects(Angle angle, double range, class_1297 entity) {
        if (SpookyTimeSmoothMode.mc.field_1724 == null || entity == null) {
            return false;
        }
        class_243 eyes = SpookyTimeSmoothMode.mc.field_1724.method_33571();
        class_238 box = BacktrackPositionResolver.boundingBox(entity);
        return box.method_1006(eyes) || box.method_992(eyes, eyes.method_1019(angle.toVector().method_1021(range))).isPresent();
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }

    private static float randomLerp(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }
}

