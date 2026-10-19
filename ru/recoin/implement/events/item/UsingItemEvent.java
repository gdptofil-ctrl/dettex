/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.item;

import ru.recoin.api.event.events.callables.EventCancellable;

public class UsingItemEvent
extends EventCancellable {
    byte type;

    public byte getType() {
        return this.type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public UsingItemEvent(byte type) {
        this.type = type;
    }
}

