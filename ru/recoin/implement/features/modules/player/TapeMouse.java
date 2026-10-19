/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.player;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.implement.events.player.TickEvent;

public class TapeMouse
extends Module {
    private final SelectSetting button = new SelectSetting("Button", "Mouse button to hold").value("Left", "Right").selected("Left");
    private final ValueSetting clickInterval = new ValueSetting("Click Interval", "Seconds between individual clicks").setValue(5.0f).range(0.0f, 100.0f).step(5.0f);
    private long nextClickAt;

    public TapeMouse() {
        super("TapeMouse", "Tape Mouse", ModuleCategory.PLAYER);
        this.setup(this.button, this.clickInterval);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (TapeMouse.mc.field_1755 != null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.nextClickAt) {
            return;
        }
        if (this.button.isSelected("Left")) {
            mc.method_1536();
        } else {
            mc.method_1583();
        }
        this.nextClickAt = now + Math.max(0L, (long)Math.round(this.clickInterval.getValue() * 1000.0f));
    }

    @Override
    public void deactivate() {
        this.nextClickAt = 0L;
        super.deactivate();
    }
}

