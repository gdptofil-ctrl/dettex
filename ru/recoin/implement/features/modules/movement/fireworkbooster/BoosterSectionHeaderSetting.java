/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.movement.fireworkbooster;

import java.util.function.Supplier;
import ru.recoin.api.feature.module.setting.Setting;

public final class BoosterSectionHeaderSetting
extends Setting {
    public BoosterSectionHeaderSetting(String name, Supplier<Boolean> visible) {
        super(name, "");
        this.setVisible(visible);
    }
}

