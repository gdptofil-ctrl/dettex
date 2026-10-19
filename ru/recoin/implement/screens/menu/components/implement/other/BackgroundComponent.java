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
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.MenuScreen;
import ru.recoin.implement.screens.menu.components.AbstractComponent;

public class BackgroundComponent
extends AbstractComponent {
    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        int accent = -8557569;
        blur.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(9.0f).softness(1.0f).thickness(1.5f).quality(50.0f).outlineColor(-14473679).color(ColorUtil.DARK_BLACK).build());
        rectangle.render(ShapeProperties.create(matrix, this.x + 1.0f, this.y + 1.0f, this.width - 2.0f, 30.0).round(9.0f, 0.0f, 9.0f, 0.0f).color(-15986924).build());
        rectangle.render(ShapeProperties.create(matrix, this.x, this.y + 30.0f, this.width, 1.0).color(-14802388).build());
        rectangle.render(ShapeProperties.create(matrix, this.x + 10.0f, this.y + 10.0f, 10.0, 10.0).round(4.0f).thickness(1.0f).outlineColor(ColorUtil.multAlpha(accent, 0.7f)).color(ColorUtil.multAlpha(accent, 0.16f)).build());
        Fonts.getMenuSize(13, Fonts.Type.BOLD).drawString(matrix, "DettexClient", this.x + 26.0f, this.y + 16.0f, -1578509);
        Fonts.getMenuSize(10).drawString(matrix, MenuScreen.INSTANCE.getCategory().getReadableName(), this.x + 26.0f, this.y + 24.0f, -7630686);
    }
}

