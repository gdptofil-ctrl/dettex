/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1802
 */
package ru.recoin.implement.features.modules.player;

import net.minecraft.class_1802;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.common.util.world.ServerUtil;
import ru.recoin.implement.events.player.TickEvent;

public class FastExp
extends Module {
    private final BooleanSetting disableInPvp = new BooleanSetting("Disable in PvP", "\u041d\u0435 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u0432\u043e \u0432\u0440\u0435\u043c\u044f \u0431\u043e\u044f").setValue(true);

    public FastExp() {
        super("FastEXP", "Fast EXP", ModuleCategory.PLAYER);
        this.setup(this.disableInPvp);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (FastExp.mc.field_1724 == null) {
            return;
        }
        if (this.disableInPvp.isValue() && (ServerUtil.isPvp() || this.isPvpBossBar())) {
            return;
        }
        if (FastExp.mc.field_1724.method_6047().method_31574(class_1802.field_8287) || FastExp.mc.field_1724.method_6079().method_31574(class_1802.field_8287)) {
            FastExp.mc.field_1752 = 0;
        }
    }

    private boolean isPvpBossBar() {
        if (FastExp.mc.field_1705 == null || FastExp.mc.field_1705.method_1740() == null) {
            return false;
        }
        return FastExp.mc.field_1705.method_1740().field_2060.values().stream().map(bar -> bar.method_5414().getString().toLowerCase()).anyMatch(text -> text.contains("\u0434\u0443\u044d\u043b\u044c") || text.contains("\u0434\u0443\u0435\u043b\u044c") || text.contains("\u0432\u044b \u0441\u0435\u0439\u0447\u0430\u0441 \u0438\u0433\u0440\u0430\u0435\u0442\u0435 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435") && text.contains("spookytime.net"));
    }
}

