/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.aim;

import java.security.SecureRandom;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.aim.AimMode;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.AngleUtil;

public class FTAimMode
extends AimMode {
    private final SecureRandom secureRandom = new SecureRandom();
    private int attackCount = 0;
    private long lastAttackTime = System.currentTimeMillis();
    private boolean aggressive = false;

    public FTAimMode() {
        super("FT");
    }

    public void setAggressive(boolean aggressive) {
        this.aggressive = aggressive;
    }

    public void onAttack() {
        ++this.attackCount;
        this.lastAttackTime = System.currentTimeMillis();
    }

    public void reset() {
        this.attackCount = 0;
        this.aggressive = false;
        this.lastAttackTime = System.currentTimeMillis();
    }

    @Override
    public Angle calculateRotation(Angle currentAngle, Angle targetAngle, class_243 aimPoint, class_1297 entity) {
        Angle delta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = delta.getYaw();
        float pitchDelta = delta.getPitch();
        float rotationDifference = (float)Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        if (rotationDifference < 1.0E-4f) {
            return new Angle(targetAngle.getYaw(), targetAngle.getPitch());
        }
        if (entity != null) {
            float speedFactor = this.aggressive ? 1.0f : (this.secureRandom.nextBoolean() ? 0.4f : 0.2f);
            float yawLimit = Math.abs(yawDelta / rotationDifference) * 180.0f;
            float pitchLimit = Math.abs(pitchDelta / rotationDifference) * 180.0f;
            float moveYaw = class_3532.method_15363((float)yawDelta, (float)(-yawLimit), (float)yawLimit);
            float movePitch = class_3532.method_15363((float)pitchDelta, (float)(-pitchLimit), (float)pitchLimit);
            Angle angle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
            angle.setYaw(class_3532.method_16439((float)this.randomRange(speedFactor, speedFactor + 0.2f), (float)currentAngle.getYaw(), (float)(currentAngle.getYaw() + moveYaw)));
            angle.setPitch(class_3532.method_15363((float)class_3532.method_16439((float)this.randomRange(speedFactor, speedFactor + 0.2f), (float)currentAngle.getPitch(), (float)(currentAngle.getPitch() + movePitch)), (float)-90.0f, (float)90.0f));
            return angle;
        }
        int modeIndex = this.attackCount % 3;
        float speedFactor = this.hasElapsed(430L) ? (this.secureRandom.nextBoolean() ? 0.4f : 0.2f) : -0.2f;
        float timeFactor = (float)this.getElapsedTime() / 40.0f + (float)(this.attackCount % 6);
        Angle randomAngle = switch (modeIndex) {
            case 0 -> new Angle((float)Math.cos(timeFactor), (float)Math.sin(timeFactor));
            case 1 -> new Angle((float)Math.sin(timeFactor), (float)Math.cos(timeFactor));
            case 2 -> new Angle((float)Math.sin(timeFactor), (float)(-Math.cos(timeFactor)));
            default -> new Angle((float)(-Math.cos(timeFactor)), (float)Math.sin(timeFactor));
        };
        float extraYaw = !this.hasElapsed(2000L) ? this.randomRange(12.0f, 24.0f) * randomAngle.getYaw() : 0.0f;
        float pitchWave = this.randomRange(0.0f, 2.0f) * (float)Math.cos((double)System.currentTimeMillis() / 5000.0);
        float extraPitch = !this.hasElapsed(2000L) ? this.randomRange(2.0f, 6.0f) * randomAngle.getPitch() + pitchWave : 0.0f;
        float yawLimit = Math.abs(yawDelta / rotationDifference) * 180.0f;
        float pitchLimit = Math.abs(pitchDelta / rotationDifference) * 180.0f;
        float moveYaw = class_3532.method_15363((float)yawDelta, (float)(-yawLimit), (float)yawLimit);
        float movePitch = class_3532.method_15363((float)pitchDelta, (float)(-pitchLimit), (float)pitchLimit);
        float lerpFactor = class_3532.method_15363((float)this.randomRange(speedFactor, speedFactor + 0.2f), (float)0.0f, (float)1.0f);
        Angle angle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
        angle.setYaw(class_3532.method_16439((float)lerpFactor, (float)currentAngle.getYaw(), (float)(currentAngle.getYaw() + moveYaw)) + extraYaw);
        angle.setPitch(class_3532.method_15363((float)(class_3532.method_16439((float)lerpFactor, (float)currentAngle.getPitch(), (float)(currentAngle.getPitch() + movePitch)) + extraPitch), (float)-90.0f, (float)90.0f));
        return angle;
    }

    private boolean hasElapsed(long delay) {
        return System.currentTimeMillis() - delay >= this.lastAttackTime;
    }

    private long getElapsedTime() {
        return System.currentTimeMillis() - this.lastAttackTime;
    }

    private float randomRange(float min, float max) {
        return class_3532.method_16439((float)this.secureRandom.nextFloat(), (float)min, (float)max);
    }
}

