/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 */
package ru.recoin.implement.events.player;

import net.minecraft.class_243;
import ru.recoin.api.event.events.Event;

public class GlidingVelocityEvent
implements Event {
    private class_243 velocity;

    public class_243 getVelocity() {
        return this.velocity;
    }

    public void setVelocity(class_243 velocity) {
        this.velocity = velocity;
    }

    public GlidingVelocityEvent(class_243 velocity) {
        this.velocity = velocity;
    }
}

