/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.component;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class HueComponent
extends AbstractComponent {
    private final ColorSetting setting;
    private boolean hueDragging;
    private float X;
    private float Y;
    private float W;
    private float H;

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        this.X = this.x + 6.0f;
        this.Y = this.y + 18.5f;
        this.W = 138.0f;
        this.H = 50.0f;
        int[] color = new int[]{-1, -16777216, Color.HSBtoRGB(this.setting.getHue(), 1.0f, 1.0f), -16777216};
        rectangle.render(ShapeProperties.create(matrix, this.X, this.Y, this.W, this.H).round(4.0f).color(color).build());
        float dotX = this.X + this.W * this.setting.getSaturation();
        float dotY = this.Y + this.H * (1.0f - this.setting.getBrightness());
        rectangle.render(ShapeProperties.create(matrix, dotX - 1.75f, dotY - 1.75f, 3.5, 3.5).round(1.75f).thickness(1.0f).outlineColor(-16777216).color(-1).build());
        float min = class_3532.method_15363((float)(((float)mouseX - this.X) / this.W), (float)0.0f, (float)1.0f);
        if (this.hueDragging) {
            this.setting.setBrightness(class_3532.method_15363((float)(1.0f - ((float)mouseY - this.Y) / this.H), (float)0.0f, (float)1.0f));
            this.setting.setSaturation(min);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.hueDragging = button == 0 && MathUtil.isHovered(mouseX, mouseY, this.X, this.Y, this.W, this.H);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.hueDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public HueComponent(ColorSetting setting) {
        this.setting = setting;
    }
}

