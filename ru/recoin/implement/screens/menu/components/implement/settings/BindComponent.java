/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  net.minecraft.class_9848
 */
package ru.recoin.implement.screens.menu.components.implement.settings;

import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_9848;
import ru.recoin.api.feature.module.setting.implement.BindSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;

public class BindComponent
extends AbstractSettingComponent {
    private final BindSetting setting;
    private boolean binding;
    private double hoverAnimation;

    public BindComponent(BindSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        String bindName = StringUtil.getBindName(this.setting.getKey());
        String name = StringUtil.ellipsize(this.binding ? "Press..." : bindName, 58.0f, 11);
        float fieldWidth = 92.0f;
        float clearWidth = 18.0f;
        float controlsWidth = fieldWidth + clearWidth;
        String description = StringUtil.ellipsize(MenuDescriptionLocalizer.localize(this.setting.getDescription()), this.width - controlsWidth - 44.0f, 10);
        boolean hovered = MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
        this.hoverAnimation = MathUtil.interpolateSmooth(4.0, this.hoverAnimation, hovered ? 1.0 : 0.0);
        float hover = (float)this.hoverAnimation;
        this.height = 34.0f;
        rectangle.render(ShapeProperties.create(matrix, this.x + this.width - controlsWidth - 10.0f, this.y + 9.0f, controlsWidth, 16.0).round(4.0f).thickness(1.0f).outlineColor(ColorUtil.overCol(-13945788, -12958118, hover)).color(ColorUtil.overCol(-15394011, -15195856, hover)).build());
        float fieldX = this.x + this.width - controlsWidth - 10.0f;
        float clearX = fieldX + fieldWidth;
        rectangle.render(ShapeProperties.create(matrix, clearX, this.y + 9.0f, 1.0, 16.0).color(-14472133).build());
        int bindingColor = this.binding ? -8288257 : class_9848.method_61324((int)255, (int)135, (int)136, (int)148);
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawCenteredString(matrix, name, fieldX + fieldWidth / 2.0f, this.y + 16.8f, bindingColor);
        Fonts.getMenuSize(12, Fonts.Type.BOLD).drawCenteredString(matrix, "x", clearX + clearWidth / 2.0f, this.y + 15.6f, -6642765);
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, DropdownMenuComponent.settingName(this.setting.getName()), this.x + 20.0f, this.y + 11.0f, -1446412);
        Fonts.getMenuSize(9).drawString(matrix, description, this.x + 20.0f, this.y + 21.0f, -10656136);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float fieldWidth = 92.0f;
            float clearWidth = 18.0f;
            float controlsWidth = fieldWidth + clearWidth;
            float fieldX = this.x + this.width - controlsWidth - 10.0f;
            float clearX = fieldX + fieldWidth;
            if (MathUtil.isHovered(mouseX, mouseY, clearX, this.y + 9.0f, clearWidth, 16.0)) {
                this.setting.setKey(-1);
                this.binding = false;
            } else {
                this.binding = MathUtil.isHovered(mouseX, mouseY, fieldX, this.y + 9.0f, fieldWidth, 16.0) ? !this.binding : false;
            }
        }
        if (this.binding && button > 1) {
            this.setting.setKey(button);
            this.binding = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.binding) {
            int key = keyCode == 261 || keyCode == 256 || keyCode == 259 ? -1 : keyCode;
            this.setting.setKey(key);
            this.binding = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

