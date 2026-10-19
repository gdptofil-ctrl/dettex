/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_437
 */
package ru.recoin.implement.events.container;

import net.minecraft.class_437;
import ru.recoin.api.event.events.Event;

public class SetScreenEvent
implements Event {
    public class_437 screen;

    public class_437 getScreen() {
        return this.screen;
    }

    public void setScreen(class_437 screen) {
        this.screen = screen;
    }

    public SetScreenEvent(class_437 screen) {
        this.screen = screen;
    }
}

