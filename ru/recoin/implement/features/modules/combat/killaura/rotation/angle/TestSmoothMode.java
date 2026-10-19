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
import ru.recoin.implement.features.modules.combat.killaura.attack.AttackHandler;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public class TestSmoothMode
extends AngleSmoothMode {
    private final SecureRandom random = new SecureRandom();
    private int lastAttackCount = -1;
    private float offsetYaw = 0.0f;
    private float offsetPitch = 0.0f;

    public TestSmoothMode() {
        super("Test");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, class_243 vec3d, class_1297 entity) {
        if (targetAngle == null) {
            return currentAngle;
        }
        AttackHandler attackHandler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
        if (attackHandler != null && attackHandler.getCount() != this.lastAttackCount) {
            this.lastAttackCount = attackHandler.getCount();
            float dir = this.random.nextFloat() * ((float)Math.PI * 2);
            float strength = 1.5f + this.random.nextFloat() * 2.5f;
            this.offsetYaw = (float)Math.cos(dir) * strength;
            this.offsetPitch = (float)Math.sin(dir) * (strength * 0.4f);
        } else {
            this.offsetYaw *= 0.7f;
            this.offsetPitch *= 0.7f;
        }
        Angle delta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = delta.getYaw() + this.offsetYaw;
        float pitchDelta = delta.getPitch() + this.offsetPitch;
        float maxYaw = 40.0f + this.random.nextFloat() * 20.0f;
        float maxPitch = 10.0f + this.random.nextFloat() * 6.0f;
        float factor = Math.min(1.0f, Math.min(maxYaw / Math.max(0.001f, Math.abs(yawDelta)), maxPitch / Math.max(0.001f, Math.abs(pitchDelta))));
        float stepYaw = yawDelta * factor;
        float stepPitch = pitchDelta * factor;
        return new Angle(currentAngle.getYaw() + stepYaw, class_3532.method_15363((float)(currentAngle.getPitch() + stepPitch), (float)-90.0f, (float)90.0f));
    }

    @Override
    public class_243 randomValue() {
        return new class_243(0.05, 0.02, 0.05);
    }
}

