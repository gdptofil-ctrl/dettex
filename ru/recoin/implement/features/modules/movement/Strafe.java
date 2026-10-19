/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.movement;

import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.implement.events.player.TickEvent;

public class Strafe
extends Module {
    private final SelectSetting mode = new SelectSetting("Mode", "Strafe mode").value("Matrix").selected("Matrix");
    private final ValueSetting speed = new ValueSetting("Speed", "Matrix strafe speed").setValue(0.42f).range(0.0f, 1.0f).visible(() -> this.mode.isSelected("Matrix"));

    public Strafe() {
        super("Strafe", "Strafe", ModuleCategory.MOVEMENT);
        this.setup(this.mode, this.speed);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (Strafe.mc.field_1724 == null || Strafe.mc.field_1687 == null) {
            return;
        }
        boolean moving = MovingUtil.hasPlayerMovement();
        if (this.mode.isSelected("Matrix")) {
            MovingUtil.setVelocity(moving ? (double)(this.speed.getValue() * 1.5f) : 0.0);
            return;
        }
    }
}

