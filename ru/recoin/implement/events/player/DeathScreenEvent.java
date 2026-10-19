/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.events.player;

import ru.recoin.api.event.events.Event;

public class DeathScreenEvent
implements Event {
    private int ticksSinceDeath;

    public int getTicksSinceDeath() {
        return this.ticksSinceDeath;
    }

    public DeathScreenEvent(int ticksSinceDeath) {
        this.ticksSinceDeath = ticksSinceDeath;
    }
}

