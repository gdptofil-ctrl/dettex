/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.feature.module;

import ru.recoin.api.feature.module.setting.implement.SelectSetting;

public interface IParticleModule {
    public SelectSetting getColorMode();

    public SelectSetting getColorAnimation();

    public int[] getCustomColors();

    default public int getHealColor() {
        return -13641408;
    }

    default public int getDamageColor() {
        return -973005;
    }
}

