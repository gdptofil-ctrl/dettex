/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.combat;

import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.util.other.Instance;

public class NoInteract
extends Module {
    public static NoInteract getInstance() {
        return Instance.get(NoInteract.class);
    }

    public NoInteract() {
        super("NoInteract", "No Interact", ModuleCategory.COMBAT);
    }
}

