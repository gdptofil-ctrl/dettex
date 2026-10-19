/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.window.implement.settings.group;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import ru.recoin.api.feature.module.setting.SettingComponentAdder;
import ru.recoin.api.feature.module.setting.implement.GroupSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.screens.menu.components.AbstractComponent;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;
import ru.recoin.implement.screens.menu.components.implement.window.AbstractWindow;

public class GroupWindow
extends AbstractWindow {
    private final List<AbstractSettingComponent> components = new ArrayList<AbstractSettingComponent>();
    private final GroupSetting setting;

    public GroupWindow(GroupSetting setting) {
        this.setting = setting;
        new SettingComponentAdder().addSettingComponent(setting.getSubSettings(), this.components);
    }

    @Override
    public void drawWindow(class_332 context, int mouseX, int mouseY, float delta) {
        Supplier<Boolean> visible;
        boolean isLimitedHeight;
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        ScissorManager scissorManager = Main.getInstance().getScissorManager();
        this.height = class_3532.method_15340((int)this.getComponentHeight(), (int)44, (int)220);
        rectangle.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(6.0f).thickness(1.0f).outlineColor(-14011322).color(-15722718).build());
        Fonts.getMenuSize(12, Fonts.Type.BOLD).drawString(matrix, DropdownMenuComponent.settingName(this.setting.getName()), this.x + 10.0f, this.y + 15.0f, -1380102);
        rectangle.render(ShapeProperties.create(matrix, this.x + 8.0f, this.y + 27.0f, this.width - 16.0f, 1.0).color(0x14FFFFFF).build());
        boolean bl = isLimitedHeight = (float)this.getComponentHeight() > this.height;
        if (isLimitedHeight) {
            scissorManager.push(matrix.method_23760().method_23761(), this.x, this.y + 31.0f, this.width, this.height - 35.0f);
        }
        float offset = this.y + 32.0f + (float)this.smoothedScroll;
        int totalHeight = 8;
        for (AbstractSettingComponent component : this.components) {
            visible = component.getSetting().getVisible();
            if (visible != null && !visible.get().booleanValue()) continue;
            float componentHeight = Math.max(component.height, 30.0f);
            component.x = this.x + 4.0f;
            component.y = offset;
            component.width = this.width - 8.0f;
            component.render(context, mouseX, mouseY, delta);
            offset += componentHeight + 2.0f;
            totalHeight += (int)componentHeight + 2;
        }
        if (isLimitedHeight) {
            scissorManager.pop();
        }
        for (int i = this.components.size() - 1; i >= 0; --i) {
            AbstractSettingComponent component;
            component = this.components.get(i);
            visible = component.getSetting().getVisible();
            if (visible != null && !visible.get().booleanValue()) continue;
            component.renderOverlay(context, mouseX, mouseY, delta);
        }
        int maxScroll = (int)Math.max(0.0f, (float)totalHeight - (this.height - 31.0f));
        this.scroll = class_3532.method_15350((double)this.scroll, (double)(-maxScroll), (double)0.0);
        this.smoothedScroll = class_3532.method_16436((double)0.1f, (double)this.smoothedScroll, (double)this.scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.draggable(MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, 19.0) && button == 0);
        boolean isAnyComponentHovered = false;
        for (AbstractSettingComponent component : this.components) {
            if (!component.isHover(mouseX, mouseY)) continue;
            isAnyComponentHovered = true;
            break;
        }
        if (isAnyComponentHovered) {
            for (AbstractSettingComponent component : this.components) {
                if (!component.isHover(mouseX, mouseY)) continue;
                component.mouseClicked(mouseX, mouseY, button);
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        for (AbstractSettingComponent component : this.components) {
            component.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        for (AbstractComponent abstractComponent : this.components) {
            if (!abstractComponent.isHover(mouseX, mouseY)) continue;
            return true;
        }
        return super.isHover(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (AbstractSettingComponent component : this.components) {
            component.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        boolean scrolled;
        boolean bl = scrolled = (float)this.getComponentHeight() > this.height && MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
        if (scrolled) {
            this.scroll += amount * 20.0;
        }
        for (AbstractSettingComponent component : this.components) {
            component.mouseScrolled(mouseX, mouseY, amount);
        }
        return scrolled;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (AbstractSettingComponent component : this.components) {
            component.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (AbstractSettingComponent component : this.components) {
            component.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    public int getComponentHeight() {
        float offsetY = 38.0f;
        for (AbstractSettingComponent component : this.components) {
            Supplier<Boolean> visible = component.getSetting().getVisible();
            if (visible != null && !visible.get().booleanValue()) continue;
            offsetY += Math.max(component.height, 30.0f) + 2.0f;
        }
        return (int)(offsetY + 8.0f);
    }

    public List<AbstractSettingComponent> getComponents() {
        return this.components;
    }

    public GroupSetting getSetting() {
        return this.setting;
    }
}

