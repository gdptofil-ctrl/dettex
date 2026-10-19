/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.render.pipeline;

final class MenuAnimationClock {
    private long lastFrameNanos;
    private double seconds;
    private double frameSeconds;
    private boolean initialized;

    MenuAnimationClock() {
    }

    float sample(long now) {
        if (!this.initialized) {
            this.initialized = true;
            this.lastFrameNanos = now;
            return (float)this.seconds;
        }
        long elapsed = now - this.lastFrameNanos;
        this.lastFrameNanos = now;
        if (elapsed <= 0L) {
            return (float)this.seconds;
        }
        if (elapsed > 250000000L) {
            this.frameSeconds = 0.0;
            return (float)this.seconds;
        }
        double delta = Math.min((double)elapsed / 1.0E9, 0.03333333333333333);
        if (this.frameSeconds == 0.0) {
            this.frameSeconds = delta;
        }
        this.frameSeconds += (delta - this.frameSeconds) * 0.12;
        this.seconds += this.frameSeconds;
        return (float)this.seconds;
    }
}

