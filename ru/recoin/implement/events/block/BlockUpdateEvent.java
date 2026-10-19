/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 */
package ru.recoin.implement.events.block;

import net.minecraft.class_2338;
import net.minecraft.class_2680;
import ru.recoin.api.event.events.Event;

public record BlockUpdateEvent(class_2680 state, class_2338 pos, Type type) implements Event
{

    public static enum Type {
        LOAD,
        UNLOAD,
        UPDATE;

    }
}

