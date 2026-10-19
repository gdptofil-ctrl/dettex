/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 */
package ru.recoin.implement.events.keyboard;

import net.minecraft.class_3675;
import net.minecraft.class_437;
import ru.recoin.api.event.events.Event;
import ru.recoin.common.QuickImports;

public record KeyEvent(class_437 screen, class_3675.class_307 type, int key, int action) implements Event,
QuickImports
{
    public boolean isKeyDown(int key) {
        return this.isKeyDown(key, KeyEvent.mc.field_1755 == null);
    }

    public boolean isKeyDown(int key, boolean screen) {
        return this.matches(key) && this.action == 1 && screen;
    }

    public boolean isKeyReleased(int key) {
        return this.isKeyReleased(key, true);
    }

    public boolean isKeyReleased(int key, boolean screen) {
        return this.matches(key) && this.action == 0 && screen;
    }

    private boolean matches(int key) {
        class_3675.class_307 expectedType = key >= 0 && key < 8 ? class_3675.class_307.field_1672 : class_3675.class_307.field_1668;
        return this.type == expectedType && this.key == key;
    }
}

