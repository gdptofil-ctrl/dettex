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
import ru.recoin.api.feature.module.setting.implement.ButtonSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.components.implement.other.ButtonComponent;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;

public class SButtonComponent
extends AbstractSettingComponent {
    private final ButtonComponent buttonComponent = new ButtonComponent();
    private final ButtonSetting setting;

    public SButtonComponent(ButtonSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        String description = StringUtil.ellipsize(MenuDescriptionLocalizer.localize(this.setting.getDescription()), this.width - 88.0f, 10);
        this.height = 34.0f;
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, DropdownMenuComponent.settingName(this.setting.getName()), this.x + 20.0f, this.y + 11.0f, -1446412);
        Fonts.getMenuSize(9).drawString(matrix, description, this.x + 20.0f, this.y + 21.0f, -10656136);
        String buttonName = this.setting.getButtonName();
        buttonName = buttonName == null || buttonName.isBlank() ? (MenuDescriptionLocalizer.isRussian() ? "\u0417\u0430\u043f\u0443\u0441\u043a" : "Run") : DropdownMenuComponent.optionName(buttonName);
        ((ButtonComponent)this.buttonComponent.setText(buttonName).setRunnable(this.setting.getRunnable()).position(this.x + this.width - 44.0f, this.y + 11.0f)).render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.buttonComponent.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

