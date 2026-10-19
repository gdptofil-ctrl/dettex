/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1268
 *  net.minecraft.class_1799
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.events.item;

import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import ru.recoin.api.event.events.Event;

public class HandOffsetEvent
implements Event {
    private class_4587 matrices;
    private class_1799 stack;
    private class_1268 hand;

    public HandOffsetEvent(class_4587 matrices, class_1799 stack, class_1268 hand) {
        this.matrices = matrices;
        this.stack = stack;
        this.hand = hand;
    }

    public class_4587 getMatrices() {
        return this.matrices;
    }

    public class_1799 getStack() {
        return this.stack;
    }

    public class_1268 getHand() {
        return this.hand;
    }

    public void setMatrices(class_4587 matrices) {
        this.matrices = matrices;
    }

    public void setStack(class_1799 stack) {
        this.stack = stack;
    }

    public void setHand(class_1268 hand) {
        this.hand = hand;
    }
}

