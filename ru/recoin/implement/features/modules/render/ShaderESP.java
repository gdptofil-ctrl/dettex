/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_1304
 *  net.minecraft.class_1308
 *  net.minecraft.class_1429
 *  net.minecraft.class_1542
 *  net.minecraft.class_1657
 */
package ru.recoin.implement.features.modules.render;

import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1308;
import net.minecraft.class_1429;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.ModuleCategory;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.other.Instance;

public final class ShaderESP
extends Module {
    private static final class_1304[] ARMOR_SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
    private final ColorSetting color = new ColorSetting("Glow Color", "Player glow color").setColor(-1200579329);
    private final SelectSetting colorMode = new SelectSetting("Color Mode", "Use theme color or a custom color").value("Theme", "Custom").selected("Theme");
    private final ValueSetting intensity = new ValueSetting("Glow Intensity", "Brightness of the shader glow").setValue(1.15f).range(0.35f, 2.0f).step(0.05f);
    private final ValueSetting radius = new ValueSetting("Glow Radius", "Framebuffer glow radius in pixels").setValue(5.0f).range(1.0f, 12.0f).step(0.5f);
    private final MultiSelectSetting targets = new MultiSelectSetting("Targets", "Entities highlighted by Shader ESP").value("Self", "Players", "Naked Players", "Mobs", "Animals", "Items").selected("Players");

    public ShaderESP() {
        super("ShaderESP", "Shader ESP", ModuleCategory.RENDER);
        this.color.visible(() -> this.colorMode.isSelected("Custom"));
        this.setup(this.targets, this.colorMode, this.color, this.radius, this.intensity);
    }

    public static ShaderESP getInstance() {
        return Instance.get(ShaderESP.class);
    }

    public boolean shouldHighlight(class_1297 entity) {
        if (!this.isState() || entity == null) {
            return false;
        }
        if (entity == ShaderESP.mc.field_1724) {
            return this.targets.isSelected("Self");
        }
        if (entity instanceof class_1657) {
            class_1657 player = (class_1657)entity;
            if (this.targets.isSelected("Players")) {
                return true;
            }
            if (!this.targets.isSelected("Naked Players")) {
                return false;
            }
            return this.isNaked(player);
        }
        if (entity instanceof class_1542) {
            return this.targets.isSelected("Items");
        }
        if (entity instanceof class_1429) {
            return this.targets.isSelected("Animals");
        }
        if (entity instanceof class_1308) {
            return this.targets.isSelected("Mobs");
        }
        return false;
    }

    private boolean isNaked(class_1657 player) {
        for (class_1304 slot : ARMOR_SLOTS) {
            if (player.method_6118(slot).method_7960()) continue;
            return false;
        }
        return true;
    }

    public float getGlowRadius() {
        return this.radius.getValue();
    }

    public float getGlowIntensity() {
        return this.intensity.getValue();
    }

    public int getOutlineColor() {
        int base = this.colorMode.isSelected("Theme") ? ColorUtil.getClientColor() : this.color.getColor();
        float pulse = 0.88f + 0.12f * (float)Math.sin((double)System.currentTimeMillis() / 230.0);
        int bright = ColorUtil.multBright(base, Math.max(0.35f, 1.0f / (this.intensity.getValue() * pulse)));
        return ColorUtil.replAlpha(bright, 255);
    }
}

