/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public final class CrystalSmoothMode
extends AngleSmoothMode {
    private final float minYawSpeed;
    private final float maxYawSpeed;
    private final float minPitchSpeed;
    private final float maxPitchSpeed;
    private float yawSpeed;
    private float pitchSpeed;

    public CrystalSmoothMode() {
        this(34.0f, 46.0f, 16.0f, 25.0f);
        this.yawSpeed = 38.0f;
        this.pitchSpeed = 20.0f;
    }

    public CrystalSmoothMode(float minYawSpeed, float maxYawSpeed, float minPitchSpeed, float maxPitchSpeed) {
        super("Crystal");
        this.minYawSpeed = minYawSpeed;
        this.maxYawSpeed = maxYawSpeed;
        this.minPitchSpeed = minPitchSpeed;
        this.maxPitchSpeed = maxPitchSpeed;
        this.yawSpeed = (minYawSpeed + maxYawSpeed) / 2.0f;
        this.pitchSpeed = (minPitchSpeed + maxPitchSpeed) / 2.0f;
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 point, class_1297 entity) {
        if (target == null) {
            return current;
        }
        if (current == null) {
            current = AngleUtil.cameraAngle();
        }
        Angle delta = AngleUtil.calculateDelta(current, target);
        this.yawSpeed += (this.random(this.minYawSpeed, this.maxYawSpeed) - this.yawSpeed) * 0.28f;
        this.pitchSpeed += (this.random(this.minPitchSpeed, this.maxPitchSpeed) - this.pitchSpeed) * 0.28f;
        float yawDistance = Math.abs(delta.getYaw());
        float yawLimit = yawDistance <= 6.0f ? Math.min(yawDistance, 1.75f) : Math.min(this.yawSpeed, Math.max(1.5f, yawDistance * 0.62f));
        float pitchLimit = Math.min(this.pitchSpeed, Math.max(1.0f, Math.abs(delta.getPitch()) * 0.58f));
        float yaw = current.getYaw() + class_3532.method_15363((float)delta.getYaw(), (float)(-yawLimit), (float)yawLimit);
        float pitch = current.getPitch() + class_3532.method_15363((float)delta.getPitch(), (float)(-pitchLimit), (float)pitchLimit);
        return new Angle(yaw, class_3532.method_15363((float)pitch, (float)-90.0f, (float)90.0f));
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }

    private float random(float min, float max) {
        return ThreadLocalRandom.current().nextFloat(min, max);
    }
}

