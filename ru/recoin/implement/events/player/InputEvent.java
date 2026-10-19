/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10185
 */
package ru.recoin.implement.events.player;

import net.minecraft.class_10185;
import ru.recoin.api.event.events.callables.EventCancellable;

public class InputEvent
extends EventCancellable {
    private class_10185 input;

    public void setJumping(boolean jump) {
        this.input = new class_10185(this.input.comp_3159(), this.input.comp_3160(), this.input.comp_3161(), this.input.comp_3162(), jump, this.input.comp_3164(), this.input.comp_3165());
    }

    public void setDirectional(boolean forward, boolean backward, boolean left, boolean right) {
        this.input = new class_10185(forward, backward, left, right, this.input.comp_3163(), this.input.comp_3164(), this.input.comp_3165());
    }

    public void inputNone() {
        this.input = new class_10185(false, false, false, false, false, false, false);
    }

    public void inputNonePreservingSneak() {
        this.input = new class_10185(false, false, false, false, false, this.input.comp_3164(), false);
    }

    public int forward() {
        return this.input.comp_3159() ? 1 : (this.input.comp_3160() ? -1 : 0);
    }

    public float sideways() {
        return this.input.comp_3161() ? 1.0f : (this.input.comp_3162() ? -1.0f : 0.0f);
    }

    public class_10185 getInput() {
        return this.input;
    }

    public void setInput(class_10185 input) {
        this.input = input;
    }

    public InputEvent(class_10185 input) {
        this.input = input;
    }
}

