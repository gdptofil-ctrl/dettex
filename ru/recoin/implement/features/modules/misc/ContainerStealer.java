/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1703
 *  net.minecraft.class_1707
 *  net.minecraft.class_1713
 *  net.minecraft.class_1735
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_437
 *  net.minecraft.class_476
 */
package ru.recoin.implement.features.modules.misc;

import java.util.Locale;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_437;
import net.minecraft.class_476;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.entity.PlayerInventoryUtil;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.implement.events.player.TickEvent;

public class ContainerStealer
extends Module {
    private final StopWatch stopWatch = new StopWatch();
    private final SelectSetting modeSetting = new SelectSetting("Stealer Type", "Selects the type of stealer").value("FunTime", "Default");
    private final ValueSetting delaySetting = new ValueSetting("Delay", "Delay between click on slot").setValue(100.0f).range(0, 1000).visible(() -> this.modeSetting.isSelected("Default"));

    public ContainerStealer() {
        super("ContainerStealer", "Container Stealer", ModuleCategory.PVE);
        this.setup(this.modeSetting, this.delaySetting);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (ContainerStealer.mc.field_1724 == null) {
            return;
        }
        switch (this.modeSetting.getSelected()) {
            case "FunTime": {
                class_1735 target;
                class_476 sh;
                String title;
                class_437 class_4372 = ContainerStealer.mc.field_1755;
                if (!(class_4372 instanceof class_476) || !(title = (sh = (class_476)class_4372).method_25440().getString().toLowerCase(Locale.ROOT)).contains("\u043c\u0438\u0441\u0442\u0438\u0447\u0435\u0441\u043a") && !title.contains("\u0441\u043e\u043a\u0440\u043e\u0432\u0438\u0449\u0435") || !this.stopWatch.every(150.0) || (target = (class_1735)((class_1707)sh.method_17577()).field_7761.stream().filter(s -> s.method_7681() && !s.field_7871.equals((Object)ContainerStealer.mc.field_1724.method_31548()) && this.isFunTimeLoot(s.method_7677())).findFirst().orElse(null)) == null) break;
                PlayerInventoryUtil.clickSlot(target, 0, class_1713.field_7794, true);
                break;
            }
            case "Default": {
                class_1735 target;
                class_1703 title = ContainerStealer.mc.field_1724.field_7512;
                if (!(title instanceof class_1707)) break;
                class_1707 sh = (class_1707)title;
                if (!this.stopWatch.every(this.delaySetting.getValue()) || (target = (class_1735)sh.field_7761.stream().filter(s -> s.method_7681() && !s.field_7871.equals((Object)ContainerStealer.mc.field_1724.method_31548())).findFirst().orElse(null)) == null) break;
                PlayerInventoryUtil.clickSlot(target, 0, class_1713.field_7794, true);
            }
        }
    }

    private boolean isFunTimeLoot(class_1799 stack) {
        if (stack == null || stack.method_7960()) {
            return false;
        }
        class_1792 item = stack.method_7909();
        return item == class_1802.field_8054 || item == class_1802.field_8614 || item == class_1802.field_8864 || item == class_1802.field_8298;
    }
}

