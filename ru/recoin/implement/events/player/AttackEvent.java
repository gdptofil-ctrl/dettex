/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_239
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_3966
 */
package ru.recoin.implement.events.player;

import net.minecraft.class_1297;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import ru.recoin.api.event.events.callables.EventCancellable;

public class AttackEvent
extends EventCancellable {
    private final class_1297 entity;
    private final boolean pre;
    private final class_243 hitPos;

    public AttackEvent(class_1297 entity) {
        this(entity, true, AttackEvent.resolveHitPos(entity));
    }

    public AttackEvent(class_1297 entity, boolean pre) {
        this(entity, pre, AttackEvent.resolveHitPos(entity));
    }

    public AttackEvent(class_1297 entity, boolean pre, class_243 hitPos) {
        this.entity = entity;
        this.pre = pre;
        this.hitPos = hitPos;
    }

    public class_1297 getEntity() {
        return this.entity;
    }

    public class_1297 getTarget() {
        return this.entity;
    }

    public boolean isPre() {
        return this.pre;
    }

    public class_243 getHitPos() {
        return this.hitPos;
    }

    private static class_243 resolveHitPos(class_1297 entity) {
        class_3966 entityHit;
        if (entity == null) {
            return null;
        }
        class_239 hitResult = class_310.method_1551().field_1765;
        if (hitResult instanceof class_3966 && (entityHit = (class_3966)hitResult).method_17782() == entity) {
            return entityHit.method_17784();
        }
        return entity.method_73189().method_1031(0.0, (double)entity.method_17682() / 2.0, 0.0);
    }
}

