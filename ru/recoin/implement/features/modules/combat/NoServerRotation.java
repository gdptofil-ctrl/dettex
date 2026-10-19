/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2596
 *  net.minecraft.class_2708
 */
package ru.recoin.implement.features.modules.combat;

import net.minecraft.class_2596;
import net.minecraft.class_2708;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.packet.PacketEvent;

public class NoServerRotation
extends Module {
    public NoServerRotation() {
        super("No Server Rotation", "No Server Rotation", ModuleCategory.COMBAT);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (NoServerRotation.mc.field_1724 == null) {
            return;
        }
        if (event.getType() == PacketEvent.Type.RECEIVE && (class_25962 = event.getPacket()) instanceof class_2708) {
            class_2708 packet = (class_2708)class_25962;
            packet.comp_3228().comp_3150 = NoServerRotation.mc.field_1724.method_36454();
            packet.comp_3228().comp_3151 = NoServerRotation.mc.field_1724.method_36455();
        }
    }
}

