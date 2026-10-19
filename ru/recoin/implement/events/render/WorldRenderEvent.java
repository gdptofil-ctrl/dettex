/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.events.render;

import net.minecraft.class_4587;
import ru.recoin.api.event.events.Event;

public class WorldRenderEvent
implements Event {
    private class_4587 stack;
    private float partialTicks;

    public WorldRenderEvent(class_4587 stack, float partialTicks) {
        this.stack = stack;
        this.partialTicks = partialTicks;
    }

    public class_4587 getStack() {
        return this.stack;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}

