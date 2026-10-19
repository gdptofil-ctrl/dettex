/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.other;

import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class ButtonComponent
extends AbstractComponent {
    private String text;
    private Runnable runnable;
    private int color = 0;
    private float hover;

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        this.width = Fonts.getMenuSize(12).getStringWidth(this.text) + 13.0f;
        this.height = 12.0f;
        int accent = this.color == 0 ? Hud.getInstance().colorSetting.getColor() : this.color;
        this.hover = MathUtil.interpolateSmooth(2.2, this.hover, MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) ? 1.0f : 0.0f);
        rectangle.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(4.0f).thickness(1.0f).outlineColor(ColorUtil.multAlpha(accent, 0.5f + 0.25f * this.hover)).color(ColorUtil.multAlpha(accent, 0.24f + 0.12f * this.hover)).build());
        Fonts.getMenuSize(12, Fonts.Type.BOLD).drawCenteredString(matrix, this.text, (double)this.x + (double)this.width / 2.0, this.y + 5.0f, -1379841);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) && button == 0) {
            this.runnable.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public ButtonComponent setText(String text) {
        this.text = text;
        return this;
    }

    public ButtonComponent setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    public ButtonComponent setColor(int color) {
        this.color = color;
        return this;
    }

    public ButtonComponent setHover(float hover) {
        this.hover = hover;
        return this;
    }
}

