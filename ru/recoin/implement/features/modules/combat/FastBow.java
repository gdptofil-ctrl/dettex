/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1657
 *  net.minecraft.class_1753
 */
package ru.recoin.implement.features.modules.combat;

import net.minecraft.class_1657;
import net.minecraft.class_1753;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.implement.events.player.TickEvent;

public class FastBow
extends Module {
    private final ValueSetting releaseDelay = new ValueSetting("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0443 \u043f\u0440\u0438 \u0441\u0442\u0440\u0435\u043b\u044c\u0431\u0435 \u0438\u0437 \u043b\u0443\u043a\u0430").setValue(3.0f).range(1.0f, 10.0f).step(1.0f);

    public FastBow() {
        super("FastBow", ModuleCategory.COMBAT);
        this.setup(this.releaseDelay);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (FastBow.mc.field_1724 != null && FastBow.mc.field_1761 != null && FastBow.mc.field_1724.method_6047().method_7909() instanceof class_1753 && FastBow.mc.field_1724.method_6115() && (float)FastBow.mc.field_1724.method_6048() >= this.releaseDelay.getValue()) {
            FastBow.mc.field_1761.method_2897((class_1657)FastBow.mc.field_1724);
        }
    }
}

