/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_1713
 *  net.minecraft.class_1802
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2868
 *  net.minecraft.class_465
 *  net.minecraft.class_8646
 */
package ru.recoin.implement.features.modules.misc;

import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2868;
import net.minecraft.class_465;
import net.minecraft.class_8646;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.TextSetting;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.other.StopWatch;
import ru.recoin.implement.events.player.RotationUpdateEvent;
import ru.recoin.implement.features.draggables.Notifications;

public class ServerJoiner
extends Module
implements QuickImports {
    private final SelectSetting mode = new SelectSetting("Mode", "Select server join mode").value("SpookyTime", "FunTime").selected("SpookyTime");
    private final TextSetting anarchy = new TextSetting("Anarchy", "Anarchy number to join (e.g. 214 -> /an214)").visible(() -> this.mode.isSelected("FunTime")).setText("214").setMax(6);
    private final StopWatch actionTimer = new StopWatch();
    static int HOTBAR_SLOT = 4;
    static int NETHERITE_SWORD_SLOT = 13;

    public ServerJoiner() {
        super("Server Joiner", ModuleCategory.MISC);
        this.setup(this.mode, this.anarchy);
    }

    @Override
    public void activate() {
        super.activate();
        this.actionTimer.reset();
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() != 0) {
            return;
        }
        if (ServerJoiner.mc.field_1724 == null || ServerJoiner.mc.field_1687 == null || ServerJoiner.mc.field_1761 == null) {
            return;
        }
        if (this.mode.isSelected("SpookyTime")) {
            this.handleSpookyTime();
        } else if (this.mode.isSelected("FunTime")) {
            this.handleFunTime();
        }
    }

    private void handleSpookyTime() {
        if (this.hasScoreboard()) {
            this.setState(false);
            Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)"\u0412\u044b \u043f\u0440\u0438\u0441\u043e\u0435\u0434\u0438\u043d\u0438\u0441\u044c \u043d\u0430 Spooky Duels"), 3000L);
            return;
        }
        if (!this.actionTimer.finished(250.0)) {
            return;
        }
        this.actionTimer.reset();
        this.selectHotbarSlot(HOTBAR_SLOT);
        if (ServerJoiner.mc.field_1755 instanceof class_465) {
            this.clickNetheriteSword();
        } else {
            this.rightClickItem();
        }
    }

    private void handleFunTime() {
        if (ServerJoiner.mc.field_1724.field_3944 == null) {
            return;
        }
        if (this.hasScoreboard()) {
            this.setState(false);
            Notifications.getInstance().addList((class_2561)class_2561.method_43470((String)"\u0412\u044b \u043f\u0440\u0438\u0441\u043e\u0435\u0434\u0438\u043d\u0438\u043b\u0438\u0441\u044c \u043d\u0430 FunTime"), 3000L);
            return;
        }
        String an = this.anarchy.getText().replaceAll("[^0-9]", "");
        if (an.isEmpty()) {
            return;
        }
        ServerJoiner.mc.field_1724.field_3944.method_45730("an" + an);
    }

    private void selectHotbarSlot(int slot) {
        if (ServerJoiner.mc.field_1724.method_31548().method_67532() == slot) {
            return;
        }
        ServerJoiner.mc.field_1724.method_31548().method_61496(slot);
        ServerJoiner.mc.field_1724.field_3944.method_52787((class_2596)new class_2868(slot));
    }

    private void rightClickItem() {
        ServerJoiner.mc.field_1761.method_2919((class_1657)ServerJoiner.mc.field_1724, class_1268.field_5808);
        ServerJoiner.mc.field_1724.method_6104(class_1268.field_5808);
    }

    private void clickNetheriteSword() {
        if (ServerJoiner.mc.field_1724.field_7512 == null) {
            return;
        }
        if (NETHERITE_SWORD_SLOT >= ServerJoiner.mc.field_1724.field_7512.field_7761.size()) {
            return;
        }
        if (!ServerJoiner.mc.field_1724.field_7512.method_7611(NETHERITE_SWORD_SLOT).method_7677().method_31574(class_1802.field_22022)) {
            return;
        }
        ServerJoiner.mc.field_1761.method_2906(ServerJoiner.mc.field_1724.field_7512.field_7763, NETHERITE_SWORD_SLOT, 0, class_1713.field_7790, (class_1657)ServerJoiner.mc.field_1724);
    }

    private boolean hasScoreboard() {
        return ServerJoiner.mc.field_1687.method_8428().method_1189(class_8646.field_45157) != null;
    }
}

