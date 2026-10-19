/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 */
package ru.recoin.implement.events.block;

import net.minecraft.class_2248;
import net.minecraft.class_2338;
import ru.recoin.api.event.events.Event;

public record PlaceBlockEvent(class_2248 block, class_2338 pos) implements Event
{
}

