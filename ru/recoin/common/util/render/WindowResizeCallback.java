/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.event.Event
 *  net.fabricmc.fabric.api.event.EventFactory
 *  net.minecraft.class_1041
 *  net.minecraft.class_310
 */
package ru.recoin.common.util.render;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.class_1041;
import net.minecraft.class_310;

public interface WindowResizeCallback {
    public static final Event<WindowResizeCallback> EVENT = EventFactory.createArrayBacked(WindowResizeCallback.class, callbacks -> (client, window) -> {
        for (WindowResizeCallback callback : callbacks) {
            callback.onResized(client, window);
        }
    });

    public void onResized(class_310 var1, class_1041 var2);
}

