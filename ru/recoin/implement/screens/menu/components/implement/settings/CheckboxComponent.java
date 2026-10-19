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
import ru.recoin.api.feature.module.setting.implement.BooleanSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.components.implement.other.CheckComponent;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;

public class CheckboxComponent
extends AbstractSettingComponent {
    private final CheckComponent checkComponent = new CheckComponent();
    private final BooleanSetting setting;
    private final Runnable toggleAction;

    public CheckboxComponent(BooleanSetting setting) {
        super(setting);
        this.setting = setting;
        this.toggleAction = () -> this.setting.setValue(!this.setting.isValue());
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        float textX = this.x + 20.0f;
        float checkX = this.x + this.width - 8.0f - 24.0f;
        float textWidth = Math.max(40.0f, checkX - textX - 10.0f);
        String description = StringUtil.ellipsize(MenuDescriptionLocalizer.localize(this.setting.getDescription()), textWidth, 9);
        this.height = 30.0f;
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, DropdownMenuComponent.settingName(this.setting.getName()), textX, this.y + 11.0f, -1446412);
        Fonts.getMenuSize(9).drawString(matrix, description, textX, this.y + 21.0f, -10656136);
        ((CheckComponent)this.checkComponent.position(checkX, this.y + 9.0f)).setRunnable(this.toggleAction).setState(this.setting.isValue()).render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.checkComponent.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }
}

