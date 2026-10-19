/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.settings.multiselect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.multiselect.MultiSelectedButton;

public class MultiSelectComponent
extends AbstractSettingComponent {
    private static final float ROW_HEIGHT = 18.0f;
    private final List<MultiSelectedButton> multiSelectedButtons = new ArrayList<MultiSelectedButton>();
    private final MultiSelectSetting setting;
    private boolean open;
    private float dropdownListX;
    private float dropDownListY;
    private float dropDownListWidth;
    private float dropDownListHeight;
    private final Animation alphaAnimation = new DecelerateAnimation().setMs(300).setValue(1.0);

    public MultiSelectComponent(MultiSelectSetting setting) {
        super(setting);
        this.setting = setting;
        this.alphaAnimation.setDirection(Direction.BACKWARDS);
        for (String s : setting.getList()) {
            this.multiSelectedButtons.add(new MultiSelectedButton(setting, s));
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrices = MatrixStackCompat.from(context.method_51448());
        String description = StringUtil.ellipsize(MenuDescriptionLocalizer.localize(this.setting.getDescription()), this.width - 144.0f, 10);
        this.height = 34.0f;
        List<String> fullSettingsList = this.setting.getList();
        this.dropdownListX = this.x + this.width - 112.0f;
        this.dropDownListY = this.y + 26.0f;
        this.dropDownListWidth = 102.0f;
        this.dropDownListHeight = (float)fullSettingsList.size() * 18.0f;
        this.height = 34.0f;
        this.alphaAnimation.setDirection(this.open ? Direction.FORWARDS : Direction.BACKWARDS);
        this.renderSelected(matrices);
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrices, DropdownMenuComponent.settingName(this.setting.getName()), this.x + 20.0f, this.y + 11.0f, -1446412);
        Fonts.getMenuSize(9).drawString(matrices, description, this.x + 20.0f, this.y + 21.0f, -10656136);
    }

    @Override
    public void renderOverlay(class_332 context, int mouseX, int mouseY, float delta) {
        if (!this.alphaAnimation.isFinished(Direction.BACKWARDS)) {
            this.renderSelectList(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (MathUtil.isHovered(mouseX, mouseY, this.dropdownListX, this.y + 9.0f, this.dropDownListWidth, 16.0)) {
                this.open = !this.open;
            } else if (this.open && !this.isHoveredList(mouseX, mouseY)) {
                this.open = false;
            }
            if (this.open) {
                this.multiSelectedButtons.forEach(selectedButton -> selectedButton.mouseClicked(mouseX, mouseY, button));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return this.open && this.isHoveredList(mouseX, mouseY);
    }

    private void renderSelected(class_4587 matrix) {
        FontRenderer font = Fonts.getMenuSize(11);
        int x1 = (int)(this.dropdownListX + 6.0f);
        float fieldY = this.y + 9.0f;
        rectangle.render(ShapeProperties.create(matrix, this.dropdownListX, fieldY, this.dropDownListWidth, 16.0).round(5.0f).thickness(1.0f).softness(0.5f).outlineColor(-1440010182).color(-871097820).build());
        String selectedText = this.setting.getSelected().stream().map(DropdownMenuComponent::optionName).collect(Collectors.joining(", "));
        String selectedName = StringUtil.ellipsize(selectedText, this.dropDownListWidth - 27.0f, 10);
        float offset = this.dropDownListWidth - 24.0f;
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), this.dropdownListX + 6.0f, fieldY, this.dropDownListWidth - 22.0f, 20.0f);
        font.drawString(matrix, selectedName, x1, (double)fieldY + 4.6, ColorUtil.getText());
        scissor.pop();
        float caretAnim = this.alphaAnimation.getOutput().floatValue();
        FontRenderer caret = Fonts.getMenuSize(23, Fonts.Type.COMBINED_ICONS);
        float caretX = this.dropdownListX + this.dropDownListWidth - 12.0f;
        float caretY = fieldY + 2.0f;
        caret.drawString(matrix, "\ue07f", caretX, caretY, ColorUtil.multAlpha(-7499100, 1.0f - caretAnim));
        caret.drawString(matrix, "\ue080", caretX, caretY, ColorUtil.multAlpha(-7499100, caretAnim));
    }

    private void renderSelectList(class_332 context, int mouseX, int mouseY, float delta) {
        float opacity = this.alphaAnimation.getOutput().floatValue();
        rectangle.render(ShapeProperties.create(MatrixStackCompat.from(context.method_51448()), this.dropdownListX, this.dropDownListY, this.dropDownListWidth, this.dropDownListHeight).round(6.0f).thickness(1.15f).softness(1.2f).outlineColor(ColorUtil.multAlpha(-13418414, opacity * 0.74f)).color(ColorUtil.multAlpha(-15656926, opacity * 0.82f)).build());
        float offset = this.dropDownListY;
        for (MultiSelectedButton button : this.multiSelectedButtons) {
            button.x = this.dropdownListX;
            button.y = offset;
            button.width = this.dropDownListWidth;
            button.height = 18.0f;
            button.setAlpha(opacity);
            button.render(context, mouseX, mouseY, delta);
            offset += 18.0f;
        }
    }

    private boolean isHoveredList(double mouseX, double mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, this.dropdownListX, this.y + 9.0f, this.dropDownListWidth, this.dropDownListHeight + 17.0f);
    }
}

