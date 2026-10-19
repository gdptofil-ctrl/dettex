/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.features.modules.movement.fireworkbooster;

public final class FireworkBoostCoefficients {
    private float xBoostMultiplier;
    private float yBoostMultiplier;
    private float zBoostMultiplier;

    public float getXBoostMultiplier() {
        return this.xBoostMultiplier;
    }

    public float getYBoostMultiplier() {
        return this.yBoostMultiplier;
    }

    public float getZBoostMultiplier() {
        return this.zBoostMultiplier;
    }

    public void setXBoostMultiplier(float xBoostMultiplier) {
        this.xBoostMultiplier = xBoostMultiplier;
    }

    public void setYBoostMultiplier(float yBoostMultiplier) {
        this.yBoostMultiplier = yBoostMultiplier;
    }

    public void setZBoostMultiplier(float zBoostMultiplier) {
        this.zBoostMultiplier = zBoostMultiplier;
    }

    public FireworkBoostCoefficients(float xBoostMultiplier, float yBoostMultiplier, float zBoostMultiplier) {
        this.xBoostMultiplier = xBoostMultiplier;
        this.yBoostMultiplier = yBoostMultiplier;
        this.zBoostMultiplier = zBoostMultiplier;
    }
}

