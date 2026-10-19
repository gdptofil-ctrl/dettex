/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10042
 */
package ru.recoin.implement.events.render;

import net.minecraft.class_10042;
import ru.recoin.api.event.events.callables.EventCancellable;

public class EntityColorEvent
extends EventCancellable {
    private int color;
    private class_10042 state;

    public EntityColorEvent(int color, class_10042 state) {
        this.color = color;
        this.state = state;
    }

    public int getColor() {
        return this.color;
    }

    public class_10042 getState() {
        return this.state;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setState(class_10042 state) {
        this.state = state;
    }
}

