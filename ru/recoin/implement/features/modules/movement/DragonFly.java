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

public class DragonFly
extends Module {
    private final ValueSetting speedSetting = new ValueSetting("Speed", "Select fly speed").setValue(2.0f).range(0.5f, 4.0f);

    public DragonFly() {
        super("DragonFly", "Dragon Fly", ModuleCategory.MOVEMENT);
        this.setup(this.speedSetting);
    }

    @EventHandler
    public void onMove(MoveEvent e) {
        if (DragonFly.mc.field_1724 == null || !DragonFly.mc.field_1724.method_31549().field_7479) {
            return;
        }
        float speed = this.speedSetting.getValue();
        float y = DragonFly.mc.field_1724.method_5715() ? -speed : (DragonFly.mc.field_1724.field_6282 ? speed : 0.0f);
        double[] motion = MovingUtil.calculateDirection(speed);
        e.setMovement(new class_243(motion[0], (double)y, motion[1]));
    }
}

