/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.chat;

import ru.recoin.api.event.events.callables.EventCancellable;

public class ChatCommandEvent
extends EventCancellable {
    private String command;

    public ChatCommandEvent(String command) {
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}

