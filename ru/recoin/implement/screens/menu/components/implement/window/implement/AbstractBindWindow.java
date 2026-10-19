/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.window.implement;

import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.components.implement.window.AbstractWindow;

public abstract class AbstractBindWindow
extends AbstractWindow {
    private boolean binding;

    protected abstract int getKey();

    protected abstract void setKey(int var1);

    protected abstract int getType();

    protected abstract void setType(int var1);

    @Override
    public void drawWindow(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        rectangle.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(4.0f).softness(25.0f).color(0x32000000).build());
        rectangle.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(4.0f).thickness(2.0f).outlineColor(ColorUtil.getOutline(0.8f, 1.0f)).color(ColorUtil.getRect(1.0f)).build());
        Fonts.getMenuSize(14).drawString(matrix, "Binding module", this.x + 5.0f, this.y + 8.0f, -1);
        image.setTexture("textures/trash.png").render(ShapeProperties.create(matrix, this.x + this.width - 13.0f, this.y + 5.3f, 8.0, 8.0).build());
        this.drawKeyButton(matrix);
        this.drawTypeButton(matrix);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float stringWidth;
            if (MathUtil.isHovered(mouseX, mouseY, this.x + this.width - 57.0f, this.y + 37.0f, 52.0, 13.0)) {
                this.setType(this.getType() != 1 ? 1 : 0);
            }
            if (MathUtil.isHovered(mouseX, mouseY, this.x + this.width - (stringWidth = Fonts.getMenuSize(14).getStringWidth(StringUtil.getBindName(this.getKey()))) - 15.0f, this.y + 18.8f, stringWidth + 10.0f, 13.0)) {
                boolean bl = this.binding = !this.binding;
            }
            if (MathUtil.isHovered(mouseX, mouseY, this.x + this.width - 13.0f, this.y + 5.3f, 8.0, 8.0)) {
                this.setKey(-1);
            }
        }
        if (this.binding && button > 1) {
            this.setKey(button);
            this.binding = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.binding) {
            int key = keyCode == 261 || keyCode == 256 || keyCode == 259 ? -1 : keyCode;
            this.setKey(key);
            this.binding = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawKeyButton(class_4587 matrix) {
        float stringWidth = Fonts.getMenuSize(14).getStringWidth(StringUtil.getBindName(this.getKey()));
        rectangle.render(ShapeProperties.create(matrix, this.x + this.width - stringWidth - 15.0f, this.y + 18.8f, stringWidth + 10.0f, 13.0).round(2.0f).thickness(2.0f).softness(1.0f).outlineColor(ColorUtil.getOutline(0.8f, 1.0f)).color(ColorUtil.getOutline(0.1f, 1.0f)).build());
        int bindingColor = this.binding ? -8288257 : -2828575;
        Fonts.getMenuSize(14).drawString(matrix, StringUtil.getBindName(this.getKey()), this.x + this.width - 10.0f - stringWidth, this.y + 23.6f, bindingColor);
        Fonts.getMenuSize(14).drawString(matrix, "Key", (int)(this.x + 5.0f), (int)((double)this.y + 24.3), -2828575);
    }

    private void drawTypeButton(class_4587 matrix) {
        rectangle.render(ShapeProperties.create(matrix, this.x + this.width - 57.0f, this.y + 37.0f, 52.0, 13.0).round(2.0f).thickness(2.0f).softness(1.0f).outlineColor(ColorUtil.getOutline(0.8f, 1.0f)).color(ColorUtil.getOutline(0.1f, 1.0f)).build());
        if (this.getType() == 1) {
            rectangle.render(ShapeProperties.create(matrix, this.x + this.width - 34.0f, this.y + 37.0f, 29.0, 13.0).round(2.0f, 2.0f, 0.0f, 0.0f).color(-8288257).build());
        } else {
            rectangle.render(ShapeProperties.create(matrix, this.x + this.width - 57.0f, this.y + 37.0f, 23.0, 13.0).round(0.0f, 0.0f, 2.0f, 2.0f).color(-8288257).build());
        }
        Fonts.getMenuSize(12).drawString(matrix, "HOLD", this.x + 52.0f, (double)this.y + 42.3, -2828575);
        Fonts.getMenuSize(12).drawString(matrix, "TOGGLE", this.x + 73.0f, (double)this.y + 42.3, -2828575);
        Fonts.getMenuSize(14).drawString(matrix, "Bind mode", (int)(this.x + 5.0f), (int)(this.y + 42.3f), -2828575);
    }
}

