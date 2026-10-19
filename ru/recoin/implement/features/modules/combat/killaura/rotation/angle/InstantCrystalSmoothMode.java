/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import ru.recoin.implement.features.modules.combat.killaura.rotation.Angle;
import ru.recoin.implement.features.modules.combat.killaura.rotation.angle.AngleSmoothMode;

public final class InstantCrystalSmoothMode
extends AngleSmoothMode {
    public InstantCrystalSmoothMode() {
        super("Instant Crystal");
    }

    @Override
    public Angle limitAngleChange(Angle current, Angle target, class_243 point, class_1297 entity) {
        if (target == null) {
            return current;
        }
        float yaw = current == null ? target.getYaw() : current.getYaw() + class_3532.method_15393((float)(target.getYaw() - current.getYaw()));
        return new Angle(yaw, class_3532.method_15363((float)target.getPitch(), (float)-90.0f, (float)90.0f));
    }

    @Override
    public class_243 randomValue() {
        return class_243.field_1353;
    }
}

