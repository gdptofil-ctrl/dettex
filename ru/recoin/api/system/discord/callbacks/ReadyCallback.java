/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Callback
 */
package ru.recoin.api.system.discord.callbacks;

import com.sun.jna.Callback;
import ru.recoin.api.system.discord.utils.DiscordUser;

public interface ReadyCallback
extends Callback {
    public void apply(DiscordUser var1);
}

