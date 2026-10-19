/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.recoin.implement.screens.menu.components.implement.settings;

import net.minecraft.class_332;
import ru.recoin.api.feature.module.setting.Setting;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public abstract class AbstractSettingComponent
extends AbstractComponent {
    private final Setting setting;

    public void renderOverlay(class_332 context, int mouseX, int mouseY, float delta) {
    }

    public Setting getSetting() {
        return this.setting;
    }

    public AbstractSettingComponent(Setting setting) {
        this.setting = setting;
    }
}

