/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.item;

import ru.recoin.api.event.events.callables.EventCancellable;

public class SwingDurationEvent
extends EventCancellable {
    private float animation;

    public float getAnimation() {
        return this.animation;
    }

    public void setAnimation(float animation) {
        this.animation = animation;
    }
}

