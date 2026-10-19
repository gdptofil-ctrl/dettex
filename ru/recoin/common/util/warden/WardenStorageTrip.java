/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.warden;

public final class WardenStorageTrip {
    private int destination;
    private Purpose purpose;
    private long retryAt;

    public void begin(int destination, Purpose purpose) {
        if (destination < 1 || destination > 999) {
            throw new IllegalArgumentException("Invalid storage anarchy");
        }
        if (this.active()) {
            return;
        }
        this.destination = destination;
        this.purpose = purpose;
        this.retryAt = 0L;
    }

    public Action advance(int currentAnarchy, boolean inPvp, long now) {
        if (!this.active()) {
            return Action.WAIT;
        }
        if (currentAnarchy == this.destination) {
            return Action.ARRIVED;
        }
        if (inPvp || now < this.retryAt) {
            return Action.WAIT;
        }
        this.retryAt = now + 5000L;
        return Action.SWITCH;
    }

    public boolean active() {
        return this.purpose != null;
    }

    public int destination() {
        return this.destination;
    }

    public Purpose purpose() {
        return this.purpose;
    }

    public void clear() {
        this.destination = 0;
        this.purpose = null;
        this.retryAt = 0L;
    }

    public static enum Purpose {
        DEPOSIT,
        RESTOCK;

    }

    public static enum Action {
        WAIT,
        SWITCH,
        ARRIVED;

    }
}

