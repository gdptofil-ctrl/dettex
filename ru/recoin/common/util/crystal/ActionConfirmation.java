/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.crystal;

public final class ActionConfirmation {
    private int sequence = -1;
    private boolean acknowledged;
    private Boolean blockResult;
    private boolean resultReceived;
    private int resultTick;

    public synchronized void acknowledge(int count) {
        if (this.sequence >= 0 && count >= this.sequence) {
            this.acknowledged = true;
        }
    }

    public synchronized void updateBlockResult(boolean flag) {
        if (this.sequence >= 0 && !Boolean.FALSE.equals(this.blockResult)) {
            this.blockResult = flag;
        }
    }

    public synchronized void begin(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("An action sequence must be non-negative");
        }
        if (count != this.sequence) {
            this.sequence = count;
            this.acknowledged = false;
            this.blockResult = null;
            this.resultReceived = false;
        }
    }

    public synchronized Status getStatus(int count, boolean flag) {
        if (this.sequence >= 0 && (this.acknowledged || this.blockResult != null)) {
            if (!this.resultReceived) {
                this.resultReceived = true;
                this.resultTick = count;
                return Status.WAITING;
            }
            if (count <= this.resultTick) {
                return Status.WAITING;
            }
            if (Boolean.FALSE.equals(this.blockResult)) {
                return Status.REJECTED;
            }
            if (flag) {
                return Status.CONFIRMED;
            }
            return this.acknowledged ? Status.REJECTED : Status.WAITING;
        }
        return Status.WAITING;
    }

    public static enum Status {
        WAITING,
        CONFIRMED,
        REJECTED;

    }
}

