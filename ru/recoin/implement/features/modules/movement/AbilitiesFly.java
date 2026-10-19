/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_243;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.implement.events.player.MoveEvent;
import ru.recoin.implement.events.player.TickEvent;

public class AbilitiesFly
extends Module {
    private final ValueSetting speedSetting = new ValueSetting("Speed", "Select fly speed").setValue(2.0f).range(0.5f, 4.0f);

    public AbilitiesFly() {
        super("Fly", "Fly", ModuleCategory.MOVEMENT);
        this.setup(this.speedSetting);
    }

    @Override
    public void deactivate() {
        if (AbilitiesFly.mc.field_1724 != null && !AbilitiesFly.mc.field_1724.method_68878() && !AbilitiesFly.mc.field_1724.method_7325()) {
            AbilitiesFly.mc.field_1724.method_31549().field_7479 = false;
            AbilitiesFly.mc.field_1724.method_31549().field_7478 = false;
        }
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AbilitiesFly.mc.field_1724 == null) {
            return;
        }
        AbilitiesFly.mc.field_1724.method_31549().field_7478 = true;
        AbilitiesFly.mc.field_1724.method_31549().field_7479 = true;
    }

    @EventHandler
    public void onMove(MoveEvent e) {
        if (AbilitiesFly.mc.field_1724 != null && AbilitiesFly.mc.field_1724.method_31549().field_7479) {
            float speed = this.speedSetting.getValue();
            float y = AbilitiesFly.mc.field_1724.method_5715() ? -speed : (AbilitiesFly.mc.field_1724.field_6282 ? speed : 0.0f);
            double[] motion = MovingUtil.calculateDirection(speed);
            e.setMovement(new class_243(motion[0], (double)y, motion[1]));
        }
    }
}

