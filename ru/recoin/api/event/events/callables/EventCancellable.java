/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.event.events.callables;

import ru.recoin.api.event.events.Cancellable;
import ru.recoin.api.event.events.Event;

public abstract class EventCancellable
implements Event,
Cancellable {
    private boolean cancelled;

    protected EventCancellable() {
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

