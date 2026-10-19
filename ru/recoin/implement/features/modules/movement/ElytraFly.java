/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1802
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2848
 *  net.minecraft.class_2848$class_2849
 *  net.minecraft.class_3532
 */
package ru.recoin.implement.features.modules.movement;

import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2848;
import net.minecraft.class_3532;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.player.TickEvent;

public class ElytraFly
extends Module {
    private transient boolean disableNextTick;

    public ElytraFly() {
        super("ElytraFly", ModuleCategory.MOVEMENT);
    }

    @Override
    public void activate() {
        this.disableNextTick = false;
        if (ElytraFly.mc.field_1724 != null) {
            ElytraFly.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"ElytraFly \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0442\u043e\u043b\u044c\u043a\u043e \u0441 \u043f\u0440\u043e\u0442\u043e\u043a\u043e\u043b\u043e\u043c 1.17.1 \u0447\u0435\u0440\u0435\u0437 ViaFabric"), false);
            if (!ElytraFly.mc.field_1724.method_24828()) {
                ElytraFly.mc.field_1724.method_7353((class_2561)class_2561.method_43470((String)"ElytraFly \u0440\u0430\u0431\u043e\u0442\u0430\u0435\u0442 \u0442\u043e\u043b\u044c\u043a\u043e \u043f\u0440\u0438 \u0432\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0438 \u043d\u0430 \u0437\u0435\u043c\u043b\u0435"), false);
                this.disableNextTick = true;
            }
        }
        super.activate();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (this.disableNextTick) {
            this.disableNextTick = false;
            this.setState(false);
            return;
        }
        if (ElytraFly.mc.field_1724 == null || ElytraFly.mc.field_1687 == null || mc.method_1562() == null || !ElytraFly.mc.field_1724.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833)) {
            return;
        }
        ElytraFly.mc.field_1724.method_36457(class_3532.method_15363((float)ElytraFly.mc.field_1724.method_36455(), (float)-30.0f, (float)30.0f));
        if (ElytraFly.mc.field_1724.method_24828()) {
            ElytraFly.mc.field_1724.method_6043();
        } else {
            if (!ElytraFly.mc.field_1724.method_6128()) {
                ElytraFly.mc.field_1724.method_23669();
                ElytraFly.mc.field_1724.field_3944.method_52787((class_2596)new class_2848((class_1297)ElytraFly.mc.field_1724, class_2848.class_2849.field_12982));
            }
            if (ElytraFly.mc.field_1724.method_6128()) {
                double yaw = Math.toRadians(ElytraFly.mc.field_1724.method_36454());
                ElytraFly.mc.field_1724.method_18800(-Math.sin(yaw) * 0.08, ElytraFly.mc.field_1724.field_6012 % 2 == 0 ? 0.29 : 0.301, Math.cos(yaw) * 0.08);
            }
        }
    }
}

