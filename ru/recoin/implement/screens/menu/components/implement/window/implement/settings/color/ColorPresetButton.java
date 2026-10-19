/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 */
package ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color;

import net.minecraft.class_332;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class ColorPresetButton
extends AbstractComponent {
    private final ColorSetting setting;
    private final int color;
    private final String theme;

    public ColorPresetButton(ColorSetting setting, int color) {
        this(setting, color, null);
    }

    public ColorPresetButton(ColorSetting setting, int color, String theme) {
        this.setting = setting;
        this.color = color;
        this.theme = theme;
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        rectangle.render(ShapeProperties.create(MatrixStackCompat.from(context.method_51448()), this.x, this.y, 8.0, 8.0).round(2.0f).color(this.color).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, this.x, this.y, 8.0, 8.0) && button == 0) {
            if (this.theme != null) {
                Hud.getInstance().applyThemePreset(this.theme);
            } else {
                this.setting.setColor(this.color);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

