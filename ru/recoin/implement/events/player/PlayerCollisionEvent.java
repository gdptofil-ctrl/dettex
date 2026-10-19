/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 */
package ru.recoin.implement.events.player;

import net.minecraft.class_2248;
import ru.recoin.api.event.events.callables.EventCancellable;

public class PlayerCollisionEvent
extends EventCancellable {
    private class_2248 block;

    public void setBlock(class_2248 block) {
        this.block = block;
    }

    public class_2248 getBlock() {
        return this.block;
    }

    public PlayerCollisionEvent(class_2248 block) {
        this.block = block;
    }
}

