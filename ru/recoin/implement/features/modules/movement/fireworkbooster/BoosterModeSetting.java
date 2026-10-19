/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.movement.fireworkbooster;

import ru.recoin.api.feature.module.setting.implement.SelectSetting;

public final class BoosterModeSetting
extends SelectSetting {
    public BoosterModeSetting(String name) {
        super(name, "");
    }

    @Override
    public void setSelected(String selected) {
        super.setSelected("ReallyWorld".equals(selected) ? "ReallyWorld 1.16.5" : selected);
    }
}

