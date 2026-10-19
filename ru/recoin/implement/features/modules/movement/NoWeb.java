/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 *  net.minecraft.class_2846
 *  net.minecraft.class_2846$class_2847
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.common.util.entity.MovingUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;

public class NoWeb
extends Module {
    private final SelectSetting mode = new SelectSetting("Mode", "No web bypass mode").value("Vanilla", "Grim", "Reallyworld").selected("Grim");

    public static NoWeb getInstance() {
        return Instance.get(NoWeb.class);
    }

    public NoWeb() {
        super("NoWeb", "No Web", ModuleCategory.MOVEMENT);
        this.setup(this.mode);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (NoWeb.mc.field_1724 == null || !PlayerIntersectionUtil.isPlayerInBlock(class_2246.field_10343)) {
            return;
        }
        if (this.mode.isSelected("Vanilla") || this.mode.isSelected("Reallyworld")) {
            return;
        }
        double[] speed = MovingUtil.calculateDirection(0.64);
        NoWeb.mc.field_1724.method_5762(speed[0], 0.0, speed[1]);
        NoWeb.mc.field_1724.field_18276.field_1351 = NoWeb.mc.field_1690.field_1903.method_1434() ? 1.2 : (NoWeb.mc.field_1690.field_1832.method_1434() ? -2.0 : 0.0);
    }

    public boolean isVanilla() {
        return this.isState() && this.mode.isSelected("Vanilla");
    }

    public boolean shouldBypassCobwebCollision() {
        return this.isState() && this.mode.isSelected("Reallyworld");
    }

    public void onCobwebCollision(class_2338 pos) {
        if (!this.shouldBypassCobwebCollision() || NoWeb.mc.field_1724 == null || NoWeb.mc.field_1687 == null || NoWeb.mc.field_1724.field_3944 == null) {
            return;
        }
        PlayerIntersectionUtil.sendSequencedPacket(sequence -> new class_2846(class_2846.class_2847.field_12973, pos, class_2350.field_11036, sequence));
    }
}

