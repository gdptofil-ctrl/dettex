/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.settings;

import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;
import ru.recoin.implement.screens.menu.components.implement.window.AbstractWindow;
import ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.ColorWindow;

public class ColorComponent
extends AbstractSettingComponent {
    private final ColorSetting setting;

    public ColorComponent(ColorSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        String description = StringUtil.ellipsize(MenuDescriptionLocalizer.localize(this.setting.getDescription()), this.width - 90.0f, 10);
        this.height = 34.0f;
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, DropdownMenuComponent.settingName(this.setting.getName()), this.x + 20.0f, this.y + 11.0f, -1446412);
        Fonts.getMenuSize(9).drawString(matrix, description, this.x + 20.0f, this.y + 21.0f, -10656136);
        rectangle.render(ShapeProperties.create(matrix, this.x + this.width - 54.0f, this.y + 9.0f, 44.0, 16.0).round(4.0f).color(this.setting.getColor()).build());
        rectangle.render(ShapeProperties.create(matrix, this.x + this.width - 54.0f, this.y + 9.0f, 44.0, 16.0).round(4.0f).thickness(1.0f).softness(0.5f).outlineColor(-13945788).color(0x1000000).build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, this.x + this.width - 54.0f, this.y + 9.0f, 44.0, 16.0) && button == 0) {
            ColorWindow existingWindow = null;
            for (AbstractWindow window : windowManager.getWindows()) {
                ColorWindow cw;
                if (!(window instanceof ColorWindow)) continue;
                existingWindow = cw = (ColorWindow)window;
                break;
            }
            if (existingWindow != null) {
                windowManager.delete(existingWindow);
                if (existingWindow.getSetting() == this.setting) {
                    return true;
                }
            }
            int posX = (int)(this.x + this.width + 6.0f);
            int posY = (int)(this.y - 20.0f);
            if (posX + 150 > mc.method_22683().method_4486()) {
                posX = (int)(this.x - 156.0f);
            }
            if (posY + 165 > mc.method_22683().method_4502()) {
                posY = mc.method_22683().method_4502() - 170;
            }
            if (posY < 10) {
                posY = 10;
            }
            AbstractWindow colorWindow = new ColorWindow(this.setting).position(posX, posY).size(150.0f, 165.0f).draggable(true);
            windowManager.add(colorWindow);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

