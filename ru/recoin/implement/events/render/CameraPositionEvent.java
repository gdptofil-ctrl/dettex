/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 */
package ru.recoin.implement.events.render;

import net.minecraft.class_243;
import ru.recoin.api.event.events.Event;

public class CameraPositionEvent
implements Event {
    private class_243 pos;

    public class_243 getPos() {
        return this.pos;
    }

    public void setPos(class_243 pos) {
        this.pos = pos;
    }

    public CameraPositionEvent(class_243 pos) {
        this.pos = pos;
    }
}

