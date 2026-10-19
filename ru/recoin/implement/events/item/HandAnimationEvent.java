/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.events.item;

import net.minecraft.class_1268;
import net.minecraft.class_4587;
import ru.recoin.api.event.events.callables.EventCancellable;

public class HandAnimationEvent
extends EventCancellable {
    private class_4587 matrices;
    private class_1268 hand;
    private float swingProgress;
    private int armSign;
    private float equipProgress;

    public HandAnimationEvent(class_4587 matrices, class_1268 hand, float swingProgress, int armSign, float equipProgress) {
        this.matrices = matrices;
        this.hand = hand;
        this.swingProgress = swingProgress;
        this.armSign = armSign;
        this.equipProgress = equipProgress;
    }

    public class_4587 getMatrices() {
        return this.matrices;
    }

    public class_1268 getHand() {
        return this.hand;
    }

    public float getSwingProgress() {
        return this.swingProgress;
    }

    public int getArmSign() {
        return this.armSign;
    }

    public float getEquipProgress() {
        return this.equipProgress;
    }

    public void setMatrices(class_4587 matrices) {
        this.matrices = matrices;
    }

    public void setHand(class_1268 hand) {
        this.hand = hand;
    }

    public void setSwingProgress(float swingProgress) {
        this.swingProgress = swingProgress;
    }

    public void setArmSign(int armSign) {
        this.armSign = armSign;
    }

    public void setEquipProgress(float equipProgress) {
        this.equipProgress = equipProgress;
    }
}

