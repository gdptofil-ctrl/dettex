/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1536
 *  net.minecraft.class_1657
 *  net.minecraft.class_1802
 *  net.minecraft.class_746
 */
package ru.recoin.implement.features.modules.player;

import java.util.Comparator;
import net.minecraft.class_1268;
import net.minecraft.class_1536;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_746;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.implement.events.player.TickEvent;

public class AutoFish
extends Module {
    private int delay;

    public AutoFish() {
        super("AutoFish", "Auto Fish", ModuleCategory.PLAYER);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_1268 rodHand;
        if (AutoFish.mc.field_1724 == null || AutoFish.mc.field_1687 == null || AutoFish.mc.field_1761 == null || AutoFish.mc.field_1755 != null) {
            return;
        }
        if (this.delay > 0) {
            --this.delay;
            return;
        }
        rodHand = AutoFish.mc.field_1724.method_6047().method_31574(class_1802.field_8378) ? class_1268.field_5808 : (AutoFish.mc.field_1724.method_6079().method_31574(class_1802.field_8378) ? class_1268.field_5810 : null);
        if (rodHand == null) {
            return;
        }
        class_1536 bobber = AutoFish.mc.field_1687.method_8390(class_1536.class, AutoFish.mc.field_1724.method_5829().method_1014(64.0), entity -> entity.method_24921() == AutoFish.mc.field_1724).stream().min(Comparator.comparingDouble(arg_0 -> ((class_746)AutoFish.mc.field_1724).method_5858(arg_0))).orElse(null);
        if (bobber == null) {
            this.use(rodHand);
            this.delay = 12;
            return;
        }
        if (bobber.method_18798().field_1351 < -0.075 && bobber.method_18798().method_37268() < 0.12) {
            this.use(rodHand);
            this.delay = 16;
        }
    }

    private void use(class_1268 hand) {
        AutoFish.mc.field_1761.method_2919((class_1657)AutoFish.mc.field_1724, hand);
        AutoFish.mc.field_1724.method_6104(hand);
    }
}

