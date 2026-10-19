/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.misc;

import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;

public class ClickGuiModule
extends Module {
    public static final int DEFAULT_KEY = 344;

    public ClickGuiModule() {
        super("ClickGUI", "Click Gui", ModuleCategory.MISC);
        this.setKey(DEFAULT_KEY);
        this.setDefaultKey(DEFAULT_KEY);
    }

    @Override
    public void switchState() {
    }

    @Override
    public void setState(boolean state) {
    }
}
