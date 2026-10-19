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
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class SaturationComponent
extends AbstractComponent {
    private final ColorSetting setting;
    private boolean saturationDragging;
    private float X;
    private float Y;
    private float W;
    private float H;

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        this.X = this.x + 6.0f;
        this.Y = this.y + 73.5f;
        this.W = 138.0f;
        this.H = 4.0f;
        float clampedX = this.X + (this.W - 3.5f) * this.setting.getHue();
        float min = class_3532.method_15363((float)(((float)mouseX - this.X) / this.W), (float)0.0f, (float)1.0f);
        image.setTexture("textures/hue.png").render(ShapeProperties.create(matrix, this.X, (double)this.Y + 0.5, this.W, this.H - 1.0f).build());
        rectangle.render(ShapeProperties.create(matrix, clampedX, this.Y + 0.25f, 3.5, 3.5).round(1.75f).thickness(1.0f).outlineColor(-16777216).color(-1).build());
        if (this.saturationDragging) {
            this.setting.setHue(min);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.saturationDragging = button == 0 && MathUtil.isHovered(mouseX, mouseY, this.X, this.Y, this.W, this.H);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.saturationDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public SaturationComponent(ColorSetting setting) {
        this.setting = setting;
    }
}

