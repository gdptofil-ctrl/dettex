/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 */
package ru.recoin.implement.events.player;

import net.minecraft.class_1297;
import ru.recoin.api.event.events.Event;

public record EntityRemovalEvent(class_1297 entity) implements Event
{
}

