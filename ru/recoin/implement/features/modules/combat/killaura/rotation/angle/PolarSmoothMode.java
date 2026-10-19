/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import java.security.SecureRandom;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public final class PolarSmoothMode
extends AngleSmoothMode {
    private final SecureRandom random = new SecureRandom();

    public PolarSmoothMode() {
        super("Polar");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 direction, class_1297 entity) {
        AttackHandler handler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
        StopWatch timer = handler.getAttackTimer();
        int hitCounter = handler.getCount();
        float factor = this.random.nextBoolean() ? 0.4f : 0.2f;
        Angle delta = AngleUtil.calculateDelta(current, target);
        float yaw = delta.getYaw();
        float pitch = delta.getPitch();
        float hypot = (float)Math.hypot(Math.abs(yaw), Math.abs(pitch));
        if (hypot < 1.0E-5f) {
            return target;
        }
        float yawLimit = Math.abs(yaw / hypot) * 180.0f;
        float pitchLimit = Math.abs(pitch / hypot) * 180.0f;
        float clampedYaw = class_3532.method_15363((float)yaw, (float)(-yawLimit), (float)yawLimit);
        float clampedPitch = class_3532.method_15363((float)pitch, (float)(-pitchLimit), (float)pitchLimit);
        Aura aura = Aura.getInstance();
        if (aura != null && aura.getTarget() != null && aura.isPolarCritReady() && (entity == null || aura.getTarget() == entity && handler.isWithinAttackRange(aura.getConfig()))) {
            return new Angle(current.getYaw() + clampedYaw, current.getPitch() + clampedPitch);
        }
        float lerp = !timer.finished(500.0) ? -0.2f : factor;
        float timeYaw = (float)(System.currentTimeMillis() % 2000L) / 75.0f;
        float timePitch = (float)(System.currentTimeMillis() % 1000L) / 75.0f;
        Angle wave = switch (hitCounter % 4) {
            case 0 -> new Angle((float)Math.cos(timeYaw), (float)Math.sin(timePitch));
            case 1 -> new Angle((float)Math.sin(timeYaw), (float)Math.cos(timePitch));
            case 2 -> new Angle((float)Math.sin(timeYaw), (float)(-Math.cos(timePitch)));
            default -> new Angle((float)(-Math.cos(timeYaw)), (float)Math.sin(timePitch));
        };
        float amountYaw = Math.clamp(this.randomLerp(lerp, lerp + 0.2f), 0.0f, 1.0f);
        float amountPitch = Math.clamp(this.randomLerp(lerp, lerp + 0.2f), 0.0f, 1.0f);
        float noiseYaw = !timer.finished(1000.0) ? this.randomLerp(6.0f, 12.0f) * wave.getYaw() : 0.0f;
        float noisePitch = !timer.finished(1000.0) ? this.randomLerp(2.0f, 4.0f) * wave.getPitch() : 0.0f;
        return new Angle(class_3532.method_16439((float)amountYaw, (float)current.getYaw(), (float)(current.getYaw() + clampedYaw)) + noiseYaw, class_3532.method_16439((float)amountPitch, (float)current.getPitch(), (float)(current.getPitch() + clampedPitch)) + noisePitch);
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }

    private float randomLerp(float min, float max) {
        return class_3532.method_16439((float)this.random.nextFloat(), (float)min, (float)max);
    }
}

