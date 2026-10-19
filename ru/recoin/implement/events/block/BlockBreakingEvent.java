/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2350
 */
package ru.recoin.implement.events.block;

import net.minecraft.class_2338;
import net.minecraft.class_2350;
import ru.recoin.api.event.events.callables.EventCancellable;

public class BlockBreakingEvent
extends EventCancellable {
    private final class_2338 blockPos;
    private final class_2350 direction;

    public BlockBreakingEvent(class_2338 blockPos, class_2350 direction) {
        this.blockPos = blockPos;
        this.direction = direction;
    }

    public class_2338 blockPos() {
        return this.blockPos;
    }

    public class_2350 direction() {
        return this.direction;
    }
}

