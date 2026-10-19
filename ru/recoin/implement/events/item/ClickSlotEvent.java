/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1713
 */
package ru.recoin.implement.events.item;

import net.minecraft.class_1713;
import ru.recoin.api.event.events.callables.EventCancellable;

public class ClickSlotEvent
extends EventCancellable {
    private int windowId;
    private int slotId;
    private int button;
    private class_1713 actionType;

    public int getWindowId() {
        return this.windowId;
    }

    public int getSlotId() {
        return this.slotId;
    }

    public int getButton() {
        return this.button;
    }

    public class_1713 getActionType() {
        return this.actionType;
    }

    public void setWindowId(int windowId) {
        this.windowId = windowId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public void setActionType(class_1713 actionType) {
        this.actionType = actionType;
    }

    public ClickSlotEvent(int windowId, int slotId, int button, class_1713 actionType) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.button = button;
        this.actionType = actionType;
    }
}

