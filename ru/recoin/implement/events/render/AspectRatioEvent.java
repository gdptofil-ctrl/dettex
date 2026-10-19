/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.render;

import ru.recoin.api.event.events.callables.EventCancellable;

public class AspectRatioEvent
extends EventCancellable {
    private float ratio;

    public float getRatio() {
        return this.ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }
}

