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
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.components.implement.other.CheckComponent;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.other.SettingComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;
import ru.recoin.implement.screens.menu.components.implement.window.AbstractWindow;
import ru.recoin.implement.screens.menu.components.implement.window.implement.settings.group.GroupWindow;

public class GroupComponent
extends AbstractSettingComponent {
    private final CheckComponent checkComponent = new CheckComponent();
    private final SettingComponent settingComponent = new SettingComponent();
    private final GroupSetting setting;

    public GroupComponent(GroupSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        String description = StringUtil.ellipsize(MenuDescriptionLocalizer.localize(this.setting.getDescription()), this.width - 76.0f, 10);
        this.height = 34.0f;
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, DropdownMenuComponent.settingName(this.setting.getName()), this.x + 20.0f, this.y + 11.0f, -1446412);
        Fonts.getMenuSize(9).drawString(matrix, description, this.x + 20.0f, this.y + 21.0f, -10656136);
        if (this.setting.isCheckbox()) {
            ((CheckComponent)this.checkComponent.position(this.x + this.width - 34.0f, this.y + 11.0f)).setRunnable(() -> this.setting.setValue(!this.setting.isValue())).setState(this.setting.isValue()).render(context, mouseX, mouseY, delta);
        }
        ((SettingComponent)this.settingComponent.position(this.x + this.width - (float)(this.setting.isCheckbox() ? 51 : 34), this.y + 13.5f)).setRunnable(() -> this.spawnWindow(mouseX, mouseY)).render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.setting.isCheckbox()) {
            this.checkComponent.mouseClicked(mouseX, mouseY, button);
        }
        this.settingComponent.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void spawnWindow(int mouseX, int mouseY) {
        AbstractWindow existingWindow = null;
        for (AbstractWindow window : windowManager.getWindows()) {
            if (!(window instanceof GroupWindow) || ((GroupWindow)window).getSetting() != this.setting) continue;
            existingWindow = window;
            break;
        }
        if (existingWindow != null) {
            windowManager.delete(existingWindow);
        } else {
            float windowWidth = 210.0f;
            float windowHeight = 23.0f;
            float windowX = Math.min((float)(mouseX + 5), (float)mc.method_22683().method_4486() - windowWidth - 8.0f);
            float windowY = Math.min(mouseY + 5, mc.method_22683().method_4502() - 220);
            AbstractWindow groupWindow = new GroupWindow(this.setting).position(Math.max(8.0f, windowX), Math.max(8.0f, windowY)).size(windowWidth, windowHeight).draggable(false);
            windowManager.add(groupWindow);
        }
    }
}

