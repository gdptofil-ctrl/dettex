/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2596
 *  net.minecraft.class_2720
 *  net.minecraft.class_2856
 *  net.minecraft.class_2856$class_2857
 */
package ru.recoin.implement.features.modules.misc;

import net.minecraft.class_2596;
import net.minecraft.class_2720;
import net.minecraft.class_2856;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.packet.PacketEvent;

public final class RPSpoofer
extends Module {
    public RPSpoofer() {
        super("RPSpoofer", "RP Spoofer", ModuleCategory.MISC);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        class_2596<?> class_25962;
        if (!event.isReceive() || !((class_25962 = event.getPacket()) instanceof class_2720)) {
            return;
        }
        class_2720 packet = (class_2720)class_25962;
        event.cancel();
        mc.execute(() -> {
            if (mc.method_1562() == null) {
                return;
            }
            mc.method_1562().method_52787((class_2596)new class_2856(packet.comp_2158(), class_2856.class_2857.field_13016));
            mc.method_1562().method_52787((class_2596)new class_2856(packet.comp_2158(), class_2856.class_2857.field_47704));
            mc.method_1562().method_52787((class_2596)new class_2856(packet.comp_2158(), class_2856.class_2857.field_13017));
        });
    }
}

