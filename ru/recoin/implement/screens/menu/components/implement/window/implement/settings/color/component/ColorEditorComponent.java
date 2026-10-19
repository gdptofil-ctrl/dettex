/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.component;

import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class ColorEditorComponent
extends AbstractComponent {
    private final ColorSetting setting;

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        int chipFill = 1614297920;
        int chipOutline = -14341322;
        int labelColor = -7170142;
        int valueColor = -920577;
        float Y = this.y + 90.5f;
        rectangle.render(ShapeProperties.create(matrix, this.x + 6.0f, Y, 31.0, 14.0).round(4.0f).thickness(1.0f).outlineColor(chipOutline).color(chipFill).build());
        Fonts.getMenuSize(11).drawCenteredString(matrix, "HEX", this.x + 21.5f, Y + 3.0f, labelColor);
        rectangle.render(ShapeProperties.create(matrix, this.x + 40.0f, Y, 80.0, 14.0).round(4.0f).thickness(1.0f).outlineColor(chipOutline).color(chipFill).build());
        String hexString = "#" + String.format("%06X", 0xFFFFFF & this.setting.getColor());
        Fonts.getMenuSize(11).drawCenteredString(matrix, hexString, this.x + 80.0f, Y + 3.0f, valueColor);
        rectangle.render(ShapeProperties.create(matrix, this.x + 122.0f, Y, 22.0, 14.0).round(4.0f).thickness(1.0f).outlineColor(chipOutline).color(chipFill).build());
        int displayValue = (int)(this.setting.getAlpha() * 100.0f);
        Fonts.getMenuSize(11).drawCenteredString(matrix, displayValue + "%", this.x + 133.0f, Y + 3.0f, valueColor);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (MathUtil.isHovered(mouseX, mouseY, this.x + 122.0f, this.y + 90.5f, 22.0, 14.0)) {
            this.setting.setAlpha(class_3532.method_15363((float)((float)((double)this.setting.getAlpha() - amount * 2.0 / 100.0)), (float)0.0f, (float)1.0f));
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public ColorEditorComponent(ColorSetting setting) {
        this.setting = setting;
    }
}

