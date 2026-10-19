/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.player;

import ru.recoin.api.event.events.Event;

public class RotationUpdateEvent
implements Event {
    byte type;

    public byte getType() {
        return this.type;
    }

    public RotationUpdateEvent(byte type) {
        this.type = type;
    }
}

