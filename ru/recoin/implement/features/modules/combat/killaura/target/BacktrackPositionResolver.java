/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.combat.killaura.target;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import ru.recoin.implement.features.modules.combat.Aura;

public final class BacktrackPositionResolver {
    private BacktrackPositionResolver() {
    }

    public static class_238 boundingBox(class_1297 entity) {
        Aura aura = Aura.getInstance();
        return aura != null && aura.isHistoricalBacktrackEnabled() && entity == aura.getTarget() ? aura.getTargetBacktrack().targetBox(entity) : entity.method_5829();
    }

    public static class_243 position(class_1297 entity) {
        return entity.method_73189().method_1019(BacktrackPositionResolver.offset(entity));
    }

    public static class_243 eyePosition(class_1297 entity) {
        return entity.method_33571().method_1019(BacktrackPositionResolver.offset(entity));
    }

    public static class_243 offset(class_1297 entity) {
        Aura aura = Aura.getInstance();
        return aura != null && aura.isHistoricalBacktrackEnabled() && entity == aura.getTarget() ? aura.getTargetBacktrack().targetOffset(entity) : class_243.field_1353;
    }
}

