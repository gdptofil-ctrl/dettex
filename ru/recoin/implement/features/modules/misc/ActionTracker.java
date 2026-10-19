/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1657
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1839
 *  net.minecraft.class_2561
 *  net.minecraft.class_5250
 */
package ru.recoin.implement.features.modules.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_124;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.common.QuickLogger;
import ru.recoin.common.util.item.PotionTrackerUtil;
import ru.recoin.implement.events.player.TickEvent;

public class ActionTracker
extends Module {
    private final Map<UUID, String> activeUse = new HashMap<UUID, String>();

    public ActionTracker() {
        super("ActionTracker", "Action Tracker", ModuleCategory.MISC);
    }

    @Override
    public void deactivate() {
        this.activeUse.clear();
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (ActionTracker.mc.field_1724 == null || ActionTracker.mc.field_1687 == null || ActionTracker.mc.field_1705 == null) {
            this.activeUse.clear();
            return;
        }
        for (class_1657 player : ActionTracker.mc.field_1687.method_18456()) {
            if (player == ActionTracker.mc.field_1724) continue;
            UUID uuid2 = player.method_5667();
            if (!player.method_6115()) {
                this.activeUse.remove(uuid2);
                continue;
            }
            class_1799 stack = player.method_6030();
            if (stack.method_7960() || !this.isTrackedAction(stack)) {
                this.activeUse.remove(uuid2);
                continue;
            }
            String signature = String.valueOf(stack.method_7909()) + "|" + stack.method_7964().getString();
            if (signature.equals(this.activeUse.get(uuid2))) continue;
            this.activeUse.put(uuid2, signature);
            this.sendUseMessage(player, stack);
        }
        this.activeUse.keySet().removeIf(uuid -> ActionTracker.mc.field_1687.method_18470(uuid) == null);
    }

    private boolean isTrackedAction(class_1799 stack) {
        class_1839 action = stack.method_7976();
        return action == class_1839.field_8950 || action == class_1839.field_8946;
    }

    private void sendUseMessage(class_1657 player, class_1799 stack) {
        class_5250 message = class_2561.method_43473().method_10852((class_2561)class_2561.method_43470((String)player.method_5477().getString()).method_27692(class_124.field_1068)).method_10852((class_2561)class_2561.method_43470((String)" \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043b ").method_27692(class_124.field_1080)).method_10852((class_2561)class_2561.method_43470((String)this.formatItemName(stack)).method_27692(class_124.field_1060));
        ActionTracker.mc.field_1705.method_1743().method_1812(QuickLogger.clientMessage((class_2561)message));
    }

    private String formatItemName(class_1799 stack) {
        if (stack.method_7909() == class_1802.field_8071) {
            return "\u0417\u043e\u043b\u043e\u0442\u0443\u044e \u043c\u043e\u0440\u043a\u043e\u0432\u044c";
        }
        if (stack.method_7909() == class_1802.field_8574) {
            return PotionTrackerUtil.getPotionDisplayName(stack);
        }
        return stack.method_7964().getString();
    }
}

