/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.component;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.components.AbstractComponent;
import ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.ColorPresetButton;

public class ColorPresetComponent
extends AbstractComponent {
    private final List<ColorPresetButton> colorPresetButtonList = new ArrayList<ColorPresetButton>();
    private final ColorSetting setting;
    private float windowHeight;
    private float windowWidth;

    public ColorPresetComponent(ColorSetting setting) {
        this.setting = setting;
        int[] presets = setting.getPresets();
        if (presets == null) {
            return;
        }
        for (int preset : presets) {
            this.colorPresetButtonList.add(new ColorPresetButton(setting, preset));
        }
        Hud hud = Hud.getInstance();
        if (hud != null && setting == hud.guiAccentColorSetting) {
            for (String theme : hud.themeSetting.getList()) {
                if ("Custom".equals(theme)) continue;
                this.colorPresetButtonList.add(new ColorPresetButton(setting, hud.themeColorFor(theme), theme));
            }
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        if (!this.colorPresetButtonList.isEmpty()) {
            Fonts.getMenuSize(11).drawString(MatrixStackCompat.from(context.method_51448()), "Prepared swatches", this.x + 6.0f, this.y + 112.0f, -7366741);
        }
        int xOffset = 0;
        int yOffset = 0;
        int colorIndex = 0;
        int size = 13;
        for (ColorPresetButton button : this.colorPresetButtonList) {
            button.x = this.x + 6.0f + (float)xOffset;
            button.y = this.y + 120.0f + (float)yOffset;
            button.render(context, mouseX, mouseY, delta);
            xOffset += size;
            if (++colorIndex < 11) continue;
            colorIndex = 0;
            xOffset = 0;
            yOffset += size - 1;
        }
        this.windowHeight = this.colorPresetButtonList.isEmpty() ? 112.0f : (float)(136 + yOffset);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.colorPresetButtonList.forEach(colorPresetButton -> colorPresetButton.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public List<ColorPresetButton> getColorPresetButtonList() {
        return this.colorPresetButtonList;
    }

    public ColorSetting getSetting() {
        return this.setting;
    }

    public float getWindowHeight() {
        return this.windowHeight;
    }

    public float getWindowWidth() {
        return this.windowWidth;
    }
}

