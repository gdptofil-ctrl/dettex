/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.recoin.api.feature.draggable;

import net.minecraft.class_332;
import ru.recoin.implement.events.container.SetScreenEvent;
import ru.recoin.implement.events.packet.PacketEvent;

public interface Draggable {
    public boolean visible();

    public void tick();

    public void render(class_332 var1, int var2, int var3, float var4);

    public void packet(PacketEvent var1);

    public void setScreen(SetScreenEvent var1);

    public boolean mouseClicked(double var1, double var3, int var5);

    public boolean mouseReleased(double var1, double var3, int var5);
}

