/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1735
 *  net.minecraft.class_332
 */
package ru.recoin.implement.events.container;

import net.minecraft.class_1735;
import net.minecraft.class_332;
import ru.recoin.api.event.events.Event;

public class HandledScreenEvent
implements Event {
    private class_332 drawContext;
    private class_1735 slotHover;
    private int backgroundWidth;
    private int backgroundHeight;
    private int mouseX;
    private int mouseY;

    public HandledScreenEvent(class_332 drawContext, class_1735 slotHover, int backgroundWidth, int backgroundHeight) {
        this(drawContext, slotHover, backgroundWidth, backgroundHeight, 0, 0);
    }

    public class_332 getDrawContext() {
        return this.drawContext;
    }

    public class_1735 getSlotHover() {
        return this.slotHover;
    }

    public int getBackgroundWidth() {
        return this.backgroundWidth;
    }

    public int getBackgroundHeight() {
        return this.backgroundHeight;
    }

    public int getMouseX() {
        return this.mouseX;
    }

    public int getMouseY() {
        return this.mouseY;
    }

    public HandledScreenEvent(class_332 drawContext, class_1735 slotHover, int backgroundWidth, int backgroundHeight, int mouseX, int mouseY) {
        this.drawContext = drawContext;
        this.slotHover = slotHover;
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
}

