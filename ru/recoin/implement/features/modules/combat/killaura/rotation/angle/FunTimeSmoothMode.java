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
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public class FunTimeSmoothMode
extends AngleSmoothMode {
    private final SecureRandom random = new SecureRandom();

    public FunTimeSmoothMode() {
        super("FunTime");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, class_243 vec3d, class_1297 entity) {
        float pitch;
        AttackHandler attackHandler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        Aura aura = Aura.getInstance();
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float)Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean canAttack = entity != null && attackHandler.canAttack(aura.getConfig(), 0);
        float yaw = canAttack ? 0.0f : (float)((double)this.randomLerp(14.0f, 18.0f) * Math.sin((double)System.currentTimeMillis() / 50.0));
        float f = pitch = canAttack ? 0.0f : (float)((double)this.randomLerp(6.0f, 8.0f) * Math.cos((double)System.currentTimeMillis() / 50.0));
        float speed = canAttack ? 1.0f : (FunTimeSmoothMode.mc.field_1724.field_6012 % 2 == 0 ? (this.random.nextBoolean() ? 0.25f : 0.1f) : 0.0f);
        float lineYaw = Math.abs(yawDelta / rotationDifference) * 50.0f;
        float linePitch = Math.abs(pitchDelta / rotationDifference) * 50.0f;
        float moveYaw = class_3532.method_15363((float)yawDelta, (float)(-lineYaw), (float)lineYaw);
        float movePitch = class_3532.method_15363((float)pitchDelta, (float)(-linePitch), (float)linePitch);
        Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(class_3532.method_16439((float)this.randomLerp(speed, speed + 0.2f), (float)currentAngle.getYaw(), (float)(currentAngle.getYaw() + moveYaw)) + yaw);
        moveAngle.setPitch(class_3532.method_16439((float)this.randomLerp(speed, speed + 0.2f), (float)currentAngle.getPitch(), (float)(currentAngle.getPitch() + movePitch)) + pitch);
        return moveAngle;
    }

    private float randomLerp(float min, float max) {
        return class_3532.method_16439((float)this.random.nextFloat(), (float)min, (float)max);
    }

    @Override
    public class_243 randomValue() {
        return new class_243(0.1, 0.1, 0.1);
    }
}

