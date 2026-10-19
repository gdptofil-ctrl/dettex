/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1799
 *  net.minecraft.class_742
 */
package ru.recoin.implement.events.render;

import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_742;
import ru.recoin.api.event.events.Event;

public class ItemRendererEvent
implements Event {
    private class_742 player;
    private class_1799 stack;
    private class_1268 hand;

    public class_742 getPlayer() {
        return this.player;
    }

    public class_1799 getStack() {
        return this.stack;
    }

    public class_1268 getHand() {
        return this.hand;
    }

    public void setPlayer(class_742 player) {
        this.player = player;
    }

    public void setStack(class_1799 stack) {
        this.stack = stack;
    }

    public void setHand(class_1268 hand) {
        this.hand = hand;
    }

    public ItemRendererEvent(class_742 player, class_1799 stack, class_1268 hand) {
        this.player = player;
        this.stack = stack;
        this.hand = hand;
    }
}

