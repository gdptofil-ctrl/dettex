/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render;

import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.common.util.other.Instance;

public class ChinaHat
extends Module {
    private final BooleanSetting friends = new BooleanSetting("Friends", "Render the hat on friends too");
    private static ChinaHat instance;

    public ChinaHat() {
        super("ChinaHat", "China Hat", ModuleCategory.RENDER);
        this.setup(this.friends);
        instance = this;
    }

    public static ChinaHat getInstance() {
        try {
            ChinaHat mod = Instance.get(ChinaHat.class);
            if (mod != null) {
                return mod;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (instance == null) {
            instance = new ChinaHat();
        }
        return instance;
    }

    public BooleanSetting getFriends() {
        return this.friends;
    }
}

