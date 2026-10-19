/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 */
package ru.recoin.implement.events.player;

import net.minecraft.class_243;
import ru.recoin.api.event.events.Event;

public class MoveEvent
implements Event {
    private class_243 movement;

    public class_243 getMovement() {
        return this.movement;
    }

    public void setMovement(class_243 movement) {
        this.movement = movement;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MoveEvent)) {
            return false;
        }
        MoveEvent other = (MoveEvent)o;
        if (!other.canEqual(this)) {
            return false;
        }
        class_243 this$movement = this.getMovement();
        class_243 other$movement = other.getMovement();
        return !(this$movement == null ? other$movement != null : !this$movement.equals(other$movement));
    }

    protected boolean canEqual(Object other) {
        return other instanceof MoveEvent;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        class_243 $movement = this.getMovement();
        result = result * 59 + ($movement == null ? 43 : $movement.hashCode());
        return result;
    }

    public String toString() {
        return "MoveEvent(movement=" + String.valueOf(this.getMovement()) + ")";
    }

    public MoveEvent(class_243 movement) {
        this.movement = movement;
    }
}

