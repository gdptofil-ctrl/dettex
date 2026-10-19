/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.implement.screens.mainmenu;

final class MenuMotion {
    private float value;

    MenuMotion() {
        this(0.0f);
    }

    MenuMotion(float value) {
        this.reset(value);
    }

    float update(float target, float smooth, float deltaSeconds) {
        this.value = MenuMotion.interpolate(this.value, target, smooth, deltaSeconds);
        if (Math.abs(this.value - target) < 1.5E-4f) {
            this.value = target;
        }
        return this.value;
    }

    static float interpolate(float value, float target, float smooth, float deltaSeconds) {
        float safeDelta = Math.min(0.033333335f, Math.max(0.0f, deltaSeconds));
        float response = 1.0f - (float)Math.exp(-20.0f * safeDelta / Math.max(0.75f, smooth));
        return value + (target - value) * response;
    }

    float value() {
        return this.value;
    }

    void reset(float value) {
        this.value = value;
    }
}

