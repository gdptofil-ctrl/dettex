/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_465
 */
package ru.recoin.implement.events.container;

import net.minecraft.class_332;
import net.minecraft.class_465;
import ru.recoin.api.event.events.Event;

public class DrawSlotsEvent
implements Event {
    private class_332 drawContext;
    private class_465<?> screen;

    public class_332 getDrawContext() {
        return this.drawContext;
    }

    public class_465<?> getScreen() {
        return this.screen;
    }

    public DrawSlotsEvent(class_332 drawContext, class_465<?> screen) {
        this.drawContext = drawContext;
        this.screen = screen;
    }
}

