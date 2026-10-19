/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 */
package ru.recoin.implement.events.player;

import net.minecraft.class_243;
import ru.recoin.api.event.events.Event;

public class PlayerVelocityStrafeEvent
implements Event {
    private final class_243 movementInput;
    private final float speed;
    private final float yaw;
    private class_243 velocity;

    public class_243 getMovementInput() {
        return this.movementInput;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float getYaw() {
        return this.yaw;
    }

    public class_243 getVelocity() {
        return this.velocity;
    }

    public void setVelocity(class_243 velocity) {
        this.velocity = velocity;
    }

    public PlayerVelocityStrafeEvent(class_243 movementInput, float speed, float yaw, class_243 velocity) {
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
        this.velocity = velocity;
    }
}

