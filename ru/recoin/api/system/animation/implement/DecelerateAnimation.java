/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.system.animation.implement;

import ru.recoin.api.system.animation.Animation;

public class DecelerateAnimation
extends Animation {
    @Override
    public double calculation(double value) {
        double x = value / (double)this.ms;
        return 1.0 - (x - 1.0) * (x - 1.0);
    }
}

