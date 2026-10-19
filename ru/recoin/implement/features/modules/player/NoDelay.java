/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.player;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;

public class NoDelay
extends Module {
    public final MultiSelectSetting ignoreSetting = new MultiSelectSetting("Type", "Allows the actions you choose").value("Jump", "Right Click", "Break CoolDown");

    public static NoDelay getInstance() {
        return Instance.get(NoDelay.class);
    }

    public NoDelay() {
        super("NoDelay", "No Delay", ModuleCategory.PLAYER);
        this.setup(this.ignoreSetting);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (this.ignoreSetting.isSelected("Break CoolDown")) {
            NoDelay.mc.field_1761.field_3716 = 0;
        }
        if (this.ignoreSetting.isSelected("Jump")) {
            NoDelay.mc.field_1724.field_6228 = 0;
        }
        if (this.ignoreSetting.isSelected("Right Click")) {
            NoDelay.mc.field_1752 = 0;
        }
    }
}

