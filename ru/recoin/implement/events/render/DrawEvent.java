/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.recoin.implement.events.render;

import net.minecraft.class_332;
import ru.recoin.api.event.events.Event;
import ru.recoin.api.system.draw.DrawEngine;

public class DrawEvent
implements Event {
    private class_332 drawContext;
    private DrawEngine drawEngine;
    private float partialTicks;

    public class_332 getDrawContext() {
        return this.drawContext;
    }

    public DrawEngine getDrawEngine() {
        return this.drawEngine;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public DrawEvent(class_332 drawContext, DrawEngine drawEngine, float partialTicks) {
        this.drawContext = drawContext;
        this.drawEngine = drawEngine;
        this.partialTicks = partialTicks;
    }
}

