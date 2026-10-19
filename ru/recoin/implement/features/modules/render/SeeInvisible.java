/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.render;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.implement.events.render.EntityColorEvent;

public class SeeInvisible
extends Module {
    private final ValueSetting alphaSetting = new ValueSetting("Alpha", "Player Alpha").setValue(0.5f).range(0.1f, 1.0f);

    public SeeInvisible() {
        super("SeeInvisible", "See Invisible", ModuleCategory.RENDER);
        this.setup(this.alphaSetting);
    }

    @EventHandler
    public void onEntityColor(EntityColorEvent e) {
        if (e.getState() == null || !e.getState().field_53333) {
            return;
        }
        e.setColor(ColorUtil.multAlpha(e.getColor(), this.alphaSetting.getValue()));
        e.cancel();
    }
}

