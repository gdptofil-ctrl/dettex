/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_1297
 *  net.minecraft.class_2561
 *  net.minecraft.class_268
 *  net.minecraft.class_5250
 *  net.minecraft.class_742
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.misc;

import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_5250;
import net.minecraft.class_742;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.player.EntityRemovalEvent;

public final class LeaveTracker
extends Module {
    public LeaveTracker() {
        super("LeaveTracker", ModuleCategory.MISC);
    }

    @EventHandler
    public void onEntityRemoval(EntityRemovalEvent event) {
        class_742 player;
        class_1297 class_12972;
        if (LeaveTracker.mc.field_1724 == null || LeaveTracker.mc.field_1687 == null || !((class_12972 = event.entity()) instanceof class_742) || (player = (class_742)class_12972) instanceof class_746 || player == LeaveTracker.mc.field_1724 || LeaveTracker.mc.field_1724.method_5739((class_1297)player) < 250.0f) {
            return;
        }
        String name = player.method_7334().name();
        if (name == null || name.isBlank() || name.contains("CIT-")) {
            return;
        }
        class_268 team = player.method_5781();
        class_5250 message = class_2561.method_43473();
        if (team != null) {
            message.method_10852(team.method_1144());
        }
        message.method_10852((class_2561)class_2561.method_43470((String)name));
        if (team != null) {
            message.method_10852(team.method_1136());
        }
        message.method_10852((class_2561)class_2561.method_43470((String)" \u0437\u0430\u043c\u0435\u0447\u0435\u043d \u043d\u0430 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u0445 ").method_27692(class_124.field_1080));
        message.method_10852((class_2561)class_2561.method_43470((String)((int)player.method_23317() + " " + (int)player.method_23318() + " " + (int)player.method_23321())).method_27692(class_124.field_1068));
        this.logDirect(new class_2561[]{message});
    }
}

