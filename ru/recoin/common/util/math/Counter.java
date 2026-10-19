/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.math;

public class Counter {
    public long lastMS = System.currentTimeMillis();

    public Counter() {
        this.resetCounter();
    }

    public static Counter create() {
        return new Counter();
    }

    public void resetCounter() {
        this.lastMS = System.currentTimeMillis();
    }

    public boolean isReached(long time) {
        return System.currentTimeMillis() - this.lastMS > time;
    }

    public void setLastMS(long newValue) {
        this.lastMS = System.currentTimeMillis() + newValue;
    }

    public void setTime(long time) {
        this.lastMS = time;
    }

    public long getTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public boolean isRunning() {
        return System.currentTimeMillis() - this.lastMS <= 0L;
    }

    public boolean hasTimeElapsed() {
        return this.lastMS < System.currentTimeMillis();
    }

    public long getLastMS() {
        return this.lastMS;
    }
}

