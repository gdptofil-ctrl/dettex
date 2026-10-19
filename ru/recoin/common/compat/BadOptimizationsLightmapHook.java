/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.compat;

import java.util.function.BooleanSupplier;
import ru.recoin.implement.features.modules.render.FullBright;

public final class BadOptimizationsLightmapHook
implements BooleanSupplier {
    @Override
    public boolean getAsBoolean() {
        FullBright fullBright = FullBright.getInstance();
        return fullBright != null && fullBright.requiresLightmapUpdate();
    }
}

