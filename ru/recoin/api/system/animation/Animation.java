/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.system.animation;

import ru.recoin.api.system.animation.AnimationCalculation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.common.util.math.Counter;

public abstract class Animation
implements AnimationCalculation {
    private final Counter counter = new Counter();
    protected int ms;
    protected double value;
    protected Direction direction = Direction.FORWARDS;

    public void reset() {
        this.counter.resetCounter();
    }

    public Animation complete() {
        this.counter.setTime(System.currentTimeMillis() - (long)this.ms - 1L);
        return this;
    }

    public boolean isDone() {
        return this.counter.isReached(this.ms);
    }

    public boolean isFinished(Direction direction) {
        return this.direction == direction && this.isDone();
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            this.adjustTimer();
        }
    }

    public boolean isDirection(Direction direction) {
        return this.direction == direction;
    }

    private void adjustTimer() {
        this.counter.setTime(System.currentTimeMillis() - ((long)this.ms - Math.min((long)this.ms, this.counter.getTime())));
    }

    public Double getOutput() {
        long elapsed = this.counter.getTime();
        if (elapsed > (long)this.ms) {
            return this.direction == Direction.FORWARDS ? this.value : 0.0;
        }
        double progress = this.calculation(elapsed);
        return this.direction == Direction.FORWARDS ? progress * this.value : (1.0 - progress) * this.value;
    }

    public Animation setMs(int ms) {
        this.ms = ms;
        return this;
    }

    public Animation setValue(double value) {
        this.value = value;
        return this;
    }
}

