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
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.implement.events.player.MoveEvent;
import ru.recoin.implement.events.player.TickEvent;

public class WaterSpeed
extends Module {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Select bypass mode").value("HVH");
    private final ValueSetting speed = new ValueSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", "\u0423\u0441\u043a\u043e\u0440\u0435\u043d\u0438\u0435 \u0432 \u0432\u043e\u0434\u0435").setValue(0.5f).range(0.1f, 2.0f).step(0.05f).visible(() -> this.modeSetting.isSelected("HVH"));

    public WaterSpeed() {
        super("WaterSpeed", "Water Speed", ModuleCategory.MOVEMENT);
        this.setup(this.modeSetting, this.speed);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (WaterSpeed.mc.field_1724 != null && WaterSpeed.mc.field_1687 != null && this.modeSetting.isSelected("HVH") && WaterSpeed.mc.field_1724.method_5799() && (WaterSpeed.mc.field_1724.method_5624() || WaterSpeed.mc.field_1724.method_5681()) && MovingUtil.hasPlayerMovement()) {
            float forward = WaterSpeed.mc.field_1724.field_3913.method_3128().field_1342;
            float sideways = WaterSpeed.mc.field_1724.field_3913.method_3128().field_1343;
            float yaw = WaterSpeed.mc.field_1724.method_36454();
            if (forward < 0.0f) {
                yaw += 180.0f;
            }
            float scale = forward < 0.0f ? -0.5f : (forward > 0.0f ? 0.5f : 1.0f);
            if (sideways > 0.0f) {
                yaw -= 90.0f * scale;
            }
            if (sideways < 0.0f) {
                yaw += 90.0f * scale;
            }
            double radians = Math.toRadians(yaw);
            WaterSpeed.mc.field_1724.method_18800(-Math.sin(radians) * (double)this.speed.getValue(), WaterSpeed.mc.field_1724.method_18798().field_1351, Math.cos(radians) * (double)this.speed.getValue());
        }
    }

    @EventHandler
    public void onMove(MoveEvent e) {
        if (WaterSpeed.mc.field_1724 != null && WaterSpeed.mc.field_1687 != null && this.modeSetting.isSelected("HVH") && WaterSpeed.mc.field_1724.method_5799()) {
            class_243 velocity = WaterSpeed.mc.field_1724.method_18798();
            e.setMovement(new class_243(velocity.field_1352, e.getMovement().field_1351, velocity.field_1350));
        }
    }
}

