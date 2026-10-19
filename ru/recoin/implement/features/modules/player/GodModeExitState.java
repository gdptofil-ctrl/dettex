/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.player;

final class GodModeExitState {
    private static final long USE_TIMEOUT = 10000000000L;
    private static final long TELEPORT_TIMEOUT = 5000000000L;
    private boolean waiting;
    private boolean usingChorus;
    private boolean consumed;
    private long useStartedAt;
    private long consumedAt;

    GodModeExitState() {
    }

    boolean isWaiting() {
        return this.waiting;
    }

    void request() {
        this.waiting = true;
        this.resetUse();
    }

    void resume() {
        this.waiting = false;
        this.resetUse();
    }

    void startUse(boolean chorus, long now) {
        if (!this.waiting) {
            return;
        }
        this.resetUse();
        this.usingChorus = chorus;
        this.useStartedAt = now;
    }

    void cancelUse() {
        if (!this.consumed) {
            this.resetUse();
        }
    }

    boolean confirmConsumption(long now) {
        if (!this.waiting || !this.usingChorus || now - this.useStartedAt > 10000000000L) {
            return false;
        }
        this.usingChorus = false;
        this.consumed = true;
        this.consumedAt = now;
        return true;
    }

    boolean acceptsTeleport(long now, boolean changedPosition) {
        return this.waiting && this.consumed && changedPosition && now - this.consumedAt <= 5000000000L;
    }

    private void resetUse() {
        this.usingChorus = false;
        this.consumed = false;
    }
}

