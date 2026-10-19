/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.chat;

import ru.recoin.api.event.events.callables.EventCancellable;

public class ChatEvent
extends EventCancellable {
    private String message;

    public ChatEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

