/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2586
 */
package ru.recoin.implement.events.block;

import net.minecraft.class_2586;
import ru.recoin.api.event.events.Event;

public record BlockEntityProgressEvent(class_2586 blockEntity, Type type) implements Event
{

    public static enum Type {
        ADD,
        REMOVE;

    }
}

