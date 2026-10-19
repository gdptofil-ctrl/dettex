/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render;

import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.other.Instance;

public final class Items2D
extends Module {
    public Items2D() {
        super("Items2D", "2D Items", ModuleCategory.RENDER);
    }

    public static Items2D getInstance() {
        return Instance.get(Items2D.class);
    }
}

