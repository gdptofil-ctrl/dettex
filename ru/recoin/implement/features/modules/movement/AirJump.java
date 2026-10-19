/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_1297;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.implement.events.player.MotionEvent;
import ru.recoin.implement.events.player.TickEvent;

public class AirJump
extends Module {
    private final SelectSetting mode = new SelectSetting("\u041e\u0431\u0445\u043e\u0434", "\u041f\u0440\u044b\u0436\u043e\u043a \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435").value("Matrix", "Default").selected("Matrix");

    public AirJump() {
        super("AirJump", ModuleCategory.MOVEMENT);
        this.setup(this.mode);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (AirJump.mc.field_1724 == null || AirJump.mc.field_1687 == null) {
            return;
        }
        if (this.mode.isSelected("Default")) {
            AirJump.mc.field_1724.method_24830(true);
        }
        if (this.mode.isSelected("Matrix") && this.canJump()) {
            AirJump.mc.field_1724.field_6017 = 0.0;
            AirJump.mc.field_1724.field_6228 = 0;
            AirJump.mc.field_1724.method_24830(true);
        }
    }

    @EventHandler
    public void onMotion(MotionEvent event) {
        if (AirJump.mc.field_1724 != null && AirJump.mc.field_1687 != null && this.mode.isSelected("Matrix") && this.canJump()) {
            event.setOnGround(true);
        }
    }

    private boolean canJump() {
        return AirJump.mc.field_1687.method_20812((class_1297)AirJump.mc.field_1724, AirJump.mc.field_1724.method_5829().method_1009(0.5, 0.0, 0.5).method_989(0.0, -1.0, 0.0)).iterator().hasNext() && AirJump.mc.field_1724.field_6012 % 2 == 0;
    }
}

