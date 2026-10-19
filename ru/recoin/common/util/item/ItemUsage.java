/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_2596
 *  net.minecraft.class_2678
 *  net.minecraft.class_2724
 *  net.minecraft.class_2799
 *  net.minecraft.class_2799$class_2800
 *  net.minecraft.class_2846
 *  net.minecraft.class_2846$class_2847
 */
package ru.recoin.common.util.item;

import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_2596;
import net.minecraft.class_2678;
import net.minecraft.class_2724;
import net.minecraft.class_2799;
import net.minecraft.class_2846;
import ru.recoin.api.event.EventHandler;
import ru.recoin.common.QuickImports;
import ru.recoin.core.Main;
import ru.recoin.implement.events.packet.PacketEvent;

public class ItemUsage
implements QuickImports {
    public static final ItemUsage INSTANCE = new ItemUsage();
    public boolean useItem;
    public boolean releaseItem = true;

    public ItemUsage() {
        Main.getInstance().getEventManager().register(this);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        class_2596<?> packet = e.getPacket();
        if (packet instanceof class_2846) {
            class_2846 player = (class_2846)packet;
            if (player.method_12363().equals(class_2846.class_2847.field_12974)) {
                this.releaseItem = true;
            }
        } else if (packet instanceof class_2799) {
            class_2799 status = (class_2799)packet;
            if (status.method_12119().equals(class_2799.class_2800.field_12774)) {
                this.releaseItem = true;
            }
        } else if (packet instanceof class_2724) {
            this.releaseItem = true;
        } else if (packet instanceof class_2678) {
            this.releaseItem = true;
        }
    }

    public void useHand(class_1268 hand) {
        if (this.releaseItem) {
            ItemUsage.mc.field_1761.method_2919((class_1657)ItemUsage.mc.field_1724, hand);
            this.releaseItem = false;
        }
        this.useItem = true;
    }

    public void setUseItem(boolean useItem) {
        this.useItem = useItem;
    }

    public void setReleaseItem(boolean releaseItem) {
        this.releaseItem = releaseItem;
    }

    public boolean isUseItem() {
        return this.useItem;
    }

    public boolean isReleaseItem() {
        return this.releaseItem;
    }
}

