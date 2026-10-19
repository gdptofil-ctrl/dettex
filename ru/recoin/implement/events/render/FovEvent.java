/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.render;

import ru.recoin.api.event.events.callables.EventCancellable;

public class FovEvent
extends EventCancellable {
    private int fov;

    public int getFov() {
        return this.fov;
    }

    public void setFov(int fov) {
        this.fov = fov;
    }
}

