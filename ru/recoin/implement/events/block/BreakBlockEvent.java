/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 */
package ru.recoin.implement.events.block;

import net.minecraft.class_2338;
import ru.recoin.api.event.events.Event;

public record BreakBlockEvent(class_2338 blockPos) implements Event
{
}

