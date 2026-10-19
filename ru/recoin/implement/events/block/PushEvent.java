/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.block;

import ru.recoin.api.event.events.callables.EventCancellable;

public class PushEvent
extends EventCancellable {
    private Type type;

    public Type getType() {
        return this.type;
    }

    public PushEvent(Type type) {
        this.type = type;
    }

    public static enum Type {
        COLLISION,
        BLOCK,
        WATER;

    }
}

