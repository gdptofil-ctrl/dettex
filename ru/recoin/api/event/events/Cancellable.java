/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.event.events;

public interface Cancellable {
    public boolean isCancelled();

    public void cancel();
}

