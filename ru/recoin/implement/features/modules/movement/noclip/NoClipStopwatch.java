/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.movement.noclip;

public final class NoClipStopwatch {
    private long startedAt = System.currentTimeMillis();

    public void reset() {
        this.startedAt = System.currentTimeMillis();
    }

    public long elapsed() {
        return System.currentTimeMillis() - this.startedAt;
    }

    public boolean hasElapsed(long delay) {
        return this.elapsed() >= delay;
    }
}

