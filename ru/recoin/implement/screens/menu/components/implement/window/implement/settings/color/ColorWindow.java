/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.module.setting.implement.ColorSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.implement.screens.menu.components.AbstractComponent;
import ru.recoin.implement.screens.menu.components.implement.window.AbstractWindow;
import ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.component.AlphaComponent;
import ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.component.ColorEditorComponent;
import ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.component.ColorPresetComponent;
import ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.component.HueComponent;
import ru.recoin.implement.screens.menu.components.implement.window.implement.settings.color.component.SaturationComponent;

public class ColorWindow
extends AbstractWindow {
    private final List<AbstractComponent> components = new ArrayList<AbstractComponent>();
    private final ColorSetting setting;
    private final HueComponent hueComponent;
    private final SaturationComponent saturationComponent;
    private final AlphaComponent alphaComponent;
    private final ColorEditorComponent colorEditorComponent;
    private final ColorPresetComponent colorPresetComponent;

    public ColorWindow(ColorSetting setting) {
        this.setting = setting;
        this.width = 150.0f;
        AbstractComponent[] abstractComponentArray = new AbstractComponent[5];
        this.hueComponent = new HueComponent(setting);
        abstractComponentArray[0] = this.hueComponent;
        this.saturationComponent = new SaturationComponent(setting);
        abstractComponentArray[1] = this.saturationComponent;
        this.alphaComponent = new AlphaComponent(setting);
        abstractComponentArray[2] = this.alphaComponent;
        this.colorEditorComponent = new ColorEditorComponent(setting);
        abstractComponentArray[3] = this.colorEditorComponent;
        this.colorPresetComponent = new ColorPresetComponent(setting);
        abstractComponentArray[4] = this.colorPresetComponent;
        this.components.addAll(Arrays.asList(abstractComponentArray));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void drawWindow(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        boolean wasHud = Render2DUtil.isHudDecorationActive();
        boolean wasGui = Render2DUtil.isGuiDecorationActive();
        Render2DUtil.setHudDecorationActive(true);
        Render2DUtil.setGuiDecorationActive(true);
        try {
            blur.setLiveMode(true);
            blur.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(8.0f).softness(5.0f).thickness(0.0f).quality(50.0f).outlineColor(0).color(-268435456).build());
        }
        finally {
            blur.setLiveMode(false);
            Render2DUtil.setHudDecorationActive(wasHud);
            Render2DUtil.setGuiDecorationActive(wasGui);
        }
        rectangle.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(8.0f).quality(16.0f).color(-2046754296).build());
        Fonts.getMenuSize(18, Fonts.Type.COMBINED_ICONS).drawString(matrix, "\ue057", this.x + 7.0f, this.y + 4.5f, -7366741);
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, "\u041f\u0430\u043b\u0438\u0442\u0440\u0430", this.x + 20.0f, this.y + 6.0f, -1973013);
        this.alphaComponent.position(this.x, this.y);
        this.hueComponent.position(this.x, this.y);
        this.saturationComponent.position(this.x, this.y);
        this.colorEditorComponent.position(this.x, this.y);
        this.height = ((ColorPresetComponent)this.colorPresetComponent.position(this.x, this.y)).getWindowHeight() + 10.0f;
        this.components.forEach(component -> component.render(context, mouseX, mouseY, delta));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.draggable(MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, 17.0));
        this.components.forEach(component -> component.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.components.forEach(component -> component.mouseScrolled(mouseX, mouseY, amount));
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.components.forEach(component -> component.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public ColorSetting getSetting() {
        return this.setting;
    }
}

