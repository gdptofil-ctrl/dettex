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
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public final class CustomSmoothMode
extends AngleSmoothMode {
    private long lookAwayStarted;
    private long nextLookAway;

    public CustomSmoothMode() {
        super("Custom");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 point, class_1297 entity) {
        if (target == null) {
            return current;
        }
        Aura aura = Aura.getInstance();
        if (aura == null) {
            return target;
        }
        Angle delta = AngleUtil.calculateDelta(current, target);
        boolean attacking = entity != null;
        float yawSpeed = attacking ? aura.getCustomAttackYawSpeed() : this.random(aura.getCustomYawSpeedMin(), aura.getCustomYawSpeedMax());
        float pitchSpeed = attacking ? aura.getCustomAttackPitchSpeed() : this.random(aura.getCustomPitchSpeedMin(), aura.getCustomPitchSpeedMax());
        float length = Math.max((float)Math.hypot(Math.abs(delta.getYaw()), Math.abs(delta.getPitch())), 1.0E-4f);
        float yawStep = Math.abs(delta.getYaw() / length) * yawSpeed;
        float pitchStep = Math.abs(delta.getPitch() / length) * pitchSpeed;
        long now = System.currentTimeMillis();
        float yawNoise = (float)Math.cos((double)now / 40.0) * aura.getCustomYawRandom() + this.random(-aura.getCustomYawRandom(), aura.getCustomYawRandom()) * 0.5f;
        float pitchNoise = (float)Math.sin((double)now / 70.0) * aura.getCustomPitchRandom() + this.random(-aura.getCustomPitchRandom(), aura.getCustomPitchRandom()) * 0.5f;
        float yaw = current.getYaw() + class_3532.method_15363((float)delta.getYaw(), (float)(-yawStep), (float)yawStep) + yawNoise;
        float pitch = current.getPitch() + class_3532.method_15363((float)delta.getPitch(), (float)(-pitchStep), (float)pitchStep) + pitchNoise;
        switch (aura.getCustomPitchFollow()) {
            case "Static": {
                float f = pitch * 0.4f;
                break;
            }
            case "Locked": {
                float f = 0.0f;
                break;
            }
            default: {
                float f = pitch = pitch;
            }
        }
        if (aura.isCustomLookAway() && this.lookAway(now, aura)) {
            pitch = -aura.getCustomLookAwayAngle();
        }
        pitch = class_3532.method_15363((float)(pitch + aura.getCustomPitchOffset()), (float)aura.getCustomPitchMin(), (float)aura.getCustomPitchMax());
        return new Angle(yaw + aura.getCustomYawOffset(), class_3532.method_15363((float)pitch, (float)-90.0f, (float)90.0f));
    }

    private boolean lookAway(long now, Aura aura) {
        if (this.nextLookAway == 0L) {
            this.nextLookAway = now + (long)(aura.getCustomLookAwayInterval() * 1000.0f);
        }
        if (this.lookAwayStarted == 0L && now >= this.nextLookAway) {
            this.lookAwayStarted = now;
            this.nextLookAway = now + (long)(aura.getCustomLookAwayInterval() * 1000.0f);
        }
        if (this.lookAwayStarted != 0L && now - this.lookAwayStarted >= 260L) {
            this.lookAwayStarted = 0L;
        }
        return this.lookAwayStarted != 0L;
    }

    private float random(float min, float max) {
        if (max <= min) {
            return min;
        }
        return (float)ThreadLocalRandom.current().nextDouble(min, max);
    }

    @Override
    public class_243 randomValue() {
        Aura aura = Aura.getInstance();
        if (aura == null) {
            return class_243.field_1353;
        }
        long now = System.currentTimeMillis();
        float speed = Math.max(0.2f, aura.getCustomOscillateSpeed());
        return new class_243(Math.sin((double)now / (250.0 / (double)speed)) * (double)aura.getCustomOscillateX(), Math.cos((double)now / (520.0 / (double)speed)) * (double)aura.getCustomOscillateY(), (double)aura.getCustomSidePointOffset());
    }
}

