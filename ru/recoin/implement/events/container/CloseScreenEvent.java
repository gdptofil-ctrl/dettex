/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_437
 */
package ru.recoin.implement.events.container;

import net.minecraft.class_437;
import ru.recoin.api.event.events.callables.EventCancellable;

public class CloseScreenEvent
extends EventCancellable {
    private class_437 screen;

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CloseScreenEvent)) {
            return false;
        }
        CloseScreenEvent other = (CloseScreenEvent)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        class_437 this$screen = this.getScreen();
        class_437 other$screen = other.getScreen();
        return !(this$screen == null ? other$screen != null : !this$screen.equals(other$screen));
    }

    protected boolean canEqual(Object other) {
        return other instanceof CloseScreenEvent;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        class_437 $screen = this.getScreen();
        result = result * 59 + ($screen == null ? 43 : $screen.hashCode());
        return result;
    }

    public class_437 getScreen() {
        return this.screen;
    }

    public void setScreen(class_437 screen) {
        this.screen = screen;
    }

    public String toString() {
        return "CloseScreenEvent(screen=" + String.valueOf(this.getScreen()) + ")";
    }

    public CloseScreenEvent(class_437 screen) {
        this.screen = screen;
    }
}

