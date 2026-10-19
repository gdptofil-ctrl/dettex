/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.common.util.animation;

import ru.recoin.common.util.animation.Interpolation;

public class ThreeStageAnimation {
    private final double appearDuration;
    private final double existDuration;
    private final double disappearDuration;
    private final double totalDuration;
    private final Interpolation appearInterpolation;
    private final Interpolation disappearInterpolation;

    public ThreeStageAnimation(double appearDuration, double existDuration, double disappearDuration, Interpolation appearInterpolation, Interpolation disappearInterpolation) {
        this.appearDuration = appearDuration;
        this.existDuration = existDuration;
        this.disappearDuration = disappearDuration;
        this.totalDuration = appearDuration + existDuration + disappearDuration;
        this.appearInterpolation = appearInterpolation;
        this.disappearInterpolation = disappearInterpolation;
    }

    public double getValue(double elapsedTime) {
        if (elapsedTime >= this.totalDuration) {
            return 0.0;
        }
        if (elapsedTime <= this.appearDuration) {
            double progress = elapsedTime / this.appearDuration;
            return this.appearInterpolation.interpolate(progress);
        }
        if (elapsedTime <= this.appearDuration + this.existDuration) {
            return 1.0;
        }
        double progress = (elapsedTime - this.appearDuration - this.existDuration) / this.disappearDuration;
        return 1.0 - this.disappearInterpolation.interpolate(progress);
    }

    public AnimationStage getStage(double elapsedTime) {
        if (elapsedTime <= this.appearDuration) {
            return AnimationStage.APPEAR;
        }
        if (elapsedTime <= this.appearDuration + this.existDuration) {
            return AnimationStage.EXIST;
        }
        if (elapsedTime <= this.totalDuration) {
            return AnimationStage.DISAPPEAR;
        }
        return AnimationStage.FINISHED;
    }

    public boolean isFinished(double elapsedTime) {
        return elapsedTime >= this.totalDuration;
    }

    public double getAppearDuration() {
        return this.appearDuration;
    }

    public double getExistDuration() {
        return this.existDuration;
    }

    public double getDisappearDuration() {
        return this.disappearDuration;
    }

    public static enum AnimationStage {
        APPEAR,
        EXIST,
        DISAPPEAR,
        FINISHED;

    }
}

