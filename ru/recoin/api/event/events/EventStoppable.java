/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.event.events;

import ru.recoin.api.event.events.Event;

public abstract class EventStoppable
implements Event {
    private boolean stopped;

    protected EventStoppable() {
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isStopped() {
        return this.stopped;
    }
}

