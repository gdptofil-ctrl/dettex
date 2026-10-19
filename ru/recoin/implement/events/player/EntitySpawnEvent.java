/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 */
package ru.recoin.implement.events.player;

import net.minecraft.class_1297;
import ru.recoin.api.event.events.callables.EventCancellable;

public class EntitySpawnEvent
extends EventCancellable {
    private class_1297 entity;

    public EntitySpawnEvent(class_1297 entity) {
        this.entity = entity;
    }

    public class_1297 getEntity() {
        return this.entity;
    }

    public void setEntity(class_1297 entity) {
        this.entity = entity;
    }
}

