/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_2246;
import net.minecraft.class_2248;
import ru.recoin.api.event.EventHandler;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.common.util.other.Instance;
import ru.recoin.implement.events.player.TickEvent;

public class NoRender
extends Module {
    public final MultiSelectSetting modeSetting = new MultiSelectSetting("Elements", "Select elements to be ignored").value("Fire", "Bad Effects", "Block Overlay", "Block Breaking Particles", "Glowing Players", "Warden Darkness", "Scoreboard", "Vignette", "Boss Bar", "Wither Hearts", "Weather", "Walls", "Sculk", "Sculk Sensors", "Sculk Particles");
    private transient int lastSculkMask = -1;

    public static NoRender getInstance() {
        return Instance.get(NoRender.class);
    }

    public NoRender() {
        super("NoRender", "No Render", ModuleCategory.RENDER);
        this.setup(this.modeSetting);
    }

    public boolean hidesSculk(class_2248 block) {
        if (!this.isState()) {
            return false;
        }
        if (this.modeSetting.isSelected("Sculk Sensors") && (block == class_2246.field_28108 || block == class_2246.field_43231)) {
            return true;
        }
        return this.modeSetting.isSelected("Sculk") && (block == class_2246.field_37568 || block == class_2246.field_37569 || block == class_2246.field_37570 || block == class_2246.field_37571);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        int mask = (this.modeSetting.isSelected("Sculk") ? 1 : 0) | (this.modeSetting.isSelected("Sculk Sensors") ? 2 : 0);
        if (mask != this.lastSculkMask) {
            this.lastSculkMask = mask;
            if (NoRender.mc.field_1687 != null) {
                NoRender.mc.field_1769.method_3279();
            }
        }
    }

    @Override
    public void activate() {
        super.activate();
        this.lastSculkMask = -1;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.lastSculkMask = -1;
        if (NoRender.mc.field_1687 != null) {
            NoRender.mc.field_1769.method_3279();
        }
    }
}

