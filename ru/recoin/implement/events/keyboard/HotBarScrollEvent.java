/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.keyboard;

import ru.recoin.api.event.events.callables.EventCancellable;

public class HotBarScrollEvent
extends EventCancellable {
    private double horizontal;
    private double vertical;

    public double getHorizontal() {
        return this.horizontal;
    }

    public double getVertical() {
        return this.vertical;
    }

    public void setHorizontal(double horizontal) {
        this.horizontal = horizontal;
    }

    public void setVertical(double vertical) {
        this.vertical = vertical;
    }

    public HotBarScrollEvent(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }
}

