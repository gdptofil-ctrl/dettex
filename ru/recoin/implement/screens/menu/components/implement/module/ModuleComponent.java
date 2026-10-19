/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  net.minecraft.class_9848
 */
package ru.recoin.implement.screens.menu.components.implement.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_9848;
import ru.recoin.api.feature.module.Module;
import ru.recoin.api.feature.module.setting.SettingComponentAdder;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.screens.menu.components.AbstractComponent;
import ru.recoin.implement.screens.menu.components.implement.other.CheckComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;

public class ModuleComponent
extends AbstractComponent {
    private static final float BIND_PILL_Y_OFFSET = 5.0f;
    private static final float BIND_TEXT_Y_OFFSET = 4.2f;
    private final List<AbstractSettingComponent> components = new ArrayList<AbstractSettingComponent>();
    private final CheckComponent checkComponent = new CheckComponent();
    private final Module module;
    private boolean binding;

    private void initialize() {
        new SettingComponentAdder().addSettingComponent(this.module.settings(), this.components);
    }

    public ModuleComponent(Module module) {
        this.module = module;
        this.initialize();
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        this.height = this.getComponentHeight();
        rectangle.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(9.0f).softness(1.0f).thickness(1.25f).outlineColor(-14473423).color(-15460321).build());
        rectangle.render(ShapeProperties.create(matrix, this.x + 1.0f, this.y + 1.0f, this.width - 2.0f, 20.0).round(9.0f, 0.0f, 9.0f, 0.0f).color(-15723751).build());
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, this.module.getVisibleName(), this.x + 10.0f, this.y + 11.0f, -1578509);
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, "Status", this.x + 10.0f, this.y + 25.0f, -2433558);
        Fonts.getMenuSize(9).drawString(matrix, this.module.isState() ? "Enabled" : "Disabled", this.x + 10.0f, this.y + 35.0f, this.module.isState() ? -6648065 : -8617582);
        ((CheckComponent)this.checkComponent.position(this.x + this.width - 24.0f, this.y + 22.0f)).setRunnable(this.module::switchState).setState(this.module.isState()).render(context, mouseX, mouseY, delta);
        this.drawBind(matrix);
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), this.x + 1.0f, this.y + 1.0f, this.width - 2.0f, this.height - 2.0f);
        float offset = this.y + 43.0f;
        for (int i = this.components.size() - 1; i >= 0; --i) {
            AbstractSettingComponent component = this.components.get(i);
            Supplier<Boolean> visible = component.getSetting().getVisible();
            if (visible != null && !visible.get().booleanValue()) continue;
            component.x = this.x;
            component.y = offset + ((float)(this.getComponentHeight() - 46) - component.height);
            component.width = this.width;
            component.render(context, mouseX, mouseY, delta);
            offset -= component.height;
        }
        scissor.pop();
    }

    public void renderOverlays(class_332 context, int mouseX, int mouseY, float delta) {
        for (int i = this.components.size() - 1; i >= 0; --i) {
            AbstractSettingComponent component = this.components.get(i);
            Supplier<Boolean> visible = component.getSetting().getVisible();
            if (visible != null && !visible.get().booleanValue()) continue;
            component.renderOverlay(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
        String bindName = StringUtil.getBindName(this.module.getKey());
        Object name = this.binding ? "(" + bindName + ") ..." : bindName;
        float stringWidth = Fonts.getMenuSize(11, Fonts.Type.BOLD).getStringWidth((String)name);
        float pillX = this.x + this.width - stringWidth - 14.0f;
        if (MathUtil.isHovered(mouseX, mouseY, pillX, this.y + 5.0f, stringWidth + 8.0f, 10.0) && button == 0) {
            this.binding = !this.binding;
        } else if (this.binding) {
            this.module.setKey(button);
            this.binding = false;
        }
        this.checkComponent.mouseClicked(mouseX, mouseY, button);
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
        return MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
    }

    @Override
    public void tick() {
        for (AbstractSettingComponent component : this.components) {
            component.tick();
        }
        super.tick();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (AbstractSettingComponent component : this.components) {
            component.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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
        for (AbstractSettingComponent component : this.components) {
            component.mouseScrolled(mouseX, mouseY, amount);
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.binding) {
            int key = keyCode == 261 || keyCode == 256 || keyCode == 259 ? -1 : keyCode;
            this.module.setKey(key);
            this.binding = false;
            return true;
        }
        for (AbstractSettingComponent component : this.components) {
            if (!component.keyPressed(keyCode, scanCode, modifiers)) continue;
            return true;
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
        float offsetY = 0.0f;
        for (AbstractSettingComponent component : this.components) {
            Supplier<Boolean> visible = component.getSetting().getVisible();
            if (visible != null && !visible.get().booleanValue()) continue;
            offsetY += component.height;
        }
        return (int)(offsetY + 46.0f);
    }

    private void drawBind(class_4587 matrix) {
        String bindName = StringUtil.getBindName(this.module.getKey());
        Object name = this.binding ? "(" + bindName + ") ..." : bindName;
        float stringWidth = Fonts.getMenuSize(11, Fonts.Type.BOLD).getStringWidth((String)name);
        float pillX = this.x + this.width - stringWidth - 14.0f;
        float pillY = this.y + 5.0f;
        float pillWidth = stringWidth + 8.0f;
        float pillHeight = 10.0f;
        rectangle.render(ShapeProperties.create(matrix, pillX, pillY, pillWidth, pillHeight).round(4.0f).thickness(1.0f).outlineColor(-14012614).color(-15065561).build());
        int bindingColor = class_9848.method_61324((int)255, (int)130, (int)136, (int)152);
        Fonts.getMenuSize(10, Fonts.Type.BOLD).drawString(matrix, (String)name, pillX + 4.0f, pillY + 4.2f, bindingColor);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ModuleComponent that = (ModuleComponent)o;
        return this.module.equals(that.module);
    }

    public int hashCode() {
        return Objects.hash(this.module);
    }

    public List<AbstractSettingComponent> getComponents() {
        return this.components;
    }

    public CheckComponent getCheckComponent() {
        return this.checkComponent;
    }

    public Module getModule() {
        return this.module;
    }

    public boolean isBinding() {
        return this.binding;
    }
}

