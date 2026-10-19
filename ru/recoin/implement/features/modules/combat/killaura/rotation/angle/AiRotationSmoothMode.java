/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.Aura;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.ai.NeuroRotation;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public final class AiRotationSmoothMode
extends AngleSmoothMode {
    public AiRotationSmoothMode() {
        super("AI");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, class_243 vec3d, class_1297 entity) {
        class_1309 living;
        block8: {
            block7: {
                Aura aura = Aura.getInstance();
                if (aura == null || targetAngle == null) {
                    return currentAngle;
                }
                if (NeuroRotation.isRecording()) {
                    return currentAngle;
                }
                if (entity == null) {
                    Angle returned = NeuroRotation.returnTo(currentAngle, targetAngle);
                    if (returned != null) {
                        return returned;
                    }
                    float yawDelta = class_3532.method_15393((float)(targetAngle.getYaw() - currentAngle.getYaw()));
                    float pitchDelta = targetAngle.getPitch() - currentAngle.getPitch();
                    return new Angle(currentAngle.getYaw() + class_3532.method_15363((float)(yawDelta * 0.28f), (float)-12.0f, (float)12.0f), class_3532.method_15363((float)(currentAngle.getPitch() + class_3532.method_15363((float)(pitchDelta * 0.28f), (float)-9.0f, (float)9.0f)), (float)-90.0f, (float)90.0f));
                }
                if (!(entity instanceof class_1309)) break block7;
                living = (class_1309)entity;
                if (NeuroRotation.ensureRunning()) break block8;
            }
            float yawDelta = class_3532.method_15393((float)(targetAngle.getYaw() - currentAngle.getYaw()));
            float pitchDelta = targetAngle.getPitch() - currentAngle.getPitch();
            return new Angle(currentAngle.getYaw() + class_3532.method_15363((float)yawDelta, (float)-35.0f, (float)35.0f), class_3532.method_15363((float)(currentAngle.getPitch() + class_3532.method_15363((float)pitchDelta, (float)-25.0f, (float)25.0f)), (float)-90.0f, (float)90.0f));
        }
        return NeuroRotation.predict(currentAngle, living);
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}
