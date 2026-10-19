/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 */
package ru.recoin.api.repository.way;

import net.minecraft.class_2338;

public record Way(String name, class_2338 pos, String server, boolean temporary) {
    public Way(String name, class_2338 pos, String server) {
        this(name, pos, server, false);
    }
}

