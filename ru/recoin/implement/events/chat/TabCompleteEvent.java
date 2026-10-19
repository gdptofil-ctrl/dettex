/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.chat;

import ru.recoin.api.event.events.callables.EventCancellable;

public class TabCompleteEvent
extends EventCancellable {
    public final String prefix;
    public String[] completions;

    public TabCompleteEvent(String prefix) {
        this.prefix = prefix;
        this.completions = null;
    }
}

