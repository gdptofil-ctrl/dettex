/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.settings.select;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import ru.recoin.api.feature.module.setting.implement.SelectSetting;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.select.SelectedButton;

public class SelectComponent
extends AbstractSettingComponent {
    private static final float PILL_HEIGHT = 18.0f;
    private static final float MIN_PILL_WIDTH = 92.0f;
    private static final float MAX_PILL_WIDTH = 148.0f;
    private static final float CARET_AREA = 13.0f;
    private static final float ROW_HEIGHT = 18.0f;
    private final List<SelectedButton> selectedButtons = new ArrayList<SelectedButton>();
    private final SelectSetting setting;
    private boolean open;
    private float pillX;
    private float pillY;
    private float pillWidth;
    private float dropdownListX;
    private float dropDownListY;
    private float dropDownListWidth;
    private float dropDownListHeight;
    private double scroll;
    private double smoothScroll;
    private final Animation alphaAnimation = new DecelerateAnimation().setMs(300).setValue(1.0);

    public SelectComponent(SelectSetting setting) {
        super(setting);
        this.setting = setting;
        this.alphaAnimation.setDirection(Direction.BACKWARDS);
        for (String s : setting.getList()) {
            this.selectedButtons.add(new SelectedButton(setting, s));
        }
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrices = MatrixStackCompat.from(context.method_51448());
        this.height = 30.0f;
        int accent = Hud.getInstance().colorSetting.getColor();
        String selectedName = DropdownMenuComponent.optionName(this.setting.getName(), this.setting.getSelected());
        float selectedWidth = Fonts.getMenuSize(10, Fonts.Type.BOLD).getStringWidth(selectedName) + 22.0f + 13.0f;
        this.pillWidth = class_3532.method_15363((float)selectedWidth, (float)92.0f, (float)Math.min(148.0f, this.width * 0.48f));
        this.pillX = this.x + this.width - 8.0f - this.pillWidth;
        this.pillY = this.y + 6.0f;
        String shown = StringUtil.ellipsize(selectedName, this.pillWidth - 14.0f - 13.0f, 10);
        float widest = this.pillWidth;
        for (String value : this.setting.getList()) {
            widest = Math.max(widest, Fonts.getMenuSize(10, Fonts.Type.BOLD).getStringWidth(DropdownMenuComponent.optionName(this.setting.getName(), value)) + 22.0f);
        }
        this.dropDownListWidth = class_3532.method_15363((float)widest, (float)92.0f, (float)Math.min(168.0f, this.width - 32.0f));
        this.dropdownListX = this.pillX + this.pillWidth - this.dropDownListWidth;
        this.dropDownListHeight = Math.min((float)this.setting.getList().size() * 18.0f, 104.0f);
        float belowY = this.pillY + 18.0f + 3.0f;
        float aboveY = this.pillY - this.dropDownListHeight - 3.0f;
        this.dropDownListY = belowY + this.dropDownListHeight > (float)(mc.method_22683().method_4502() - 44) && aboveY > 8.0f ? aboveY : belowY;
        this.alphaAnimation.setDirection(this.open ? Direction.FORWARDS : Direction.BACKWARDS);
        float descWidth = Math.max(20.0f, this.pillX - (this.x + 20.0f) - 6.0f);
        String description = StringUtil.ellipsize(MenuDescriptionLocalizer.localize(this.setting.getDescription()), descWidth, 9);
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrices, DropdownMenuComponent.settingName(this.setting.getName()), this.x + 20.0f, this.y + 11.0f, -1446412);
        Fonts.getMenuSize(9).drawString(matrices, description, this.x + 20.0f, this.y + 21.0f, -10656136);
        this.renderPill(matrices, shown, accent);
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
            if (MathUtil.isHovered(mouseX, mouseY, this.pillX, this.pillY, this.pillWidth, 18.0)) {
                this.open = !this.open;
            } else if (this.open && !this.isHoveredList(mouseX, mouseY)) {
                this.open = false;
            }
            if (this.open) {
                this.selectedButtons.forEach(selectedButton -> selectedButton.mouseClicked(mouseX, mouseY, button));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return this.open && this.isHoveredList(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.open && this.isHoveredList(mouseX, mouseY)) {
            this.scroll += amount * 18.0;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private void renderPill(class_4587 matrices, String shown, int accent) {
        rectangle.render(ShapeProperties.create(matrices, this.pillX, this.pillY, this.pillWidth, 18.0).round(5.5f).thickness(1.0f).outlineColor(this.open ? ColorUtil.multAlpha(accent, 0.75f) : 0x14FFFFFF).color(-15591389).build());
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrices.method_23760().method_23761(), this.pillX + 8.0f, this.pillY, this.pillWidth - 13.0f - 8.0f, 18.0f);
        Fonts.getMenuSize(10, Fonts.Type.BOLD).drawString(matrices, shown, this.pillX + 8.0f, this.pillY + 6.0f, -1446412);
        scissor.pop();
        this.drawCaret(matrices, this.pillX + this.pillWidth - 14.0f, this.pillY + 3.5f, accent);
    }

    private void drawCaret(class_4587 matrices, float cx, float ty, int accent) {
        float anim = this.alphaAnimation.getOutput().floatValue();
        int color = ColorUtil.overCol(-7499100, ColorUtil.multAlpha(accent, 0.95f), anim);
        Fonts.getMenuSize(23, Fonts.Type.COMBINED_ICONS).drawString(matrices, "\ue07f", cx, ty, ColorUtil.multAlpha(color, 1.0f - anim));
        Fonts.getMenuSize(23, Fonts.Type.COMBINED_ICONS).drawString(matrices, "\ue080", cx, ty, ColorUtil.multAlpha(color, anim));
    }

    private void renderSelectList(class_332 context, int mouseX, int mouseY, float delta) {
        float opacity = this.alphaAnimation.getOutput().floatValue();
        float contentHeight = (float)this.selectedButtons.size() * 18.0f;
        float maxScroll = Math.max(0.0f, contentHeight - this.dropDownListHeight);
        this.scroll = class_3532.method_15350((double)this.scroll, (double)(-maxScroll), (double)0.0);
        this.smoothScroll = MathUtil.interpolateSmooth(2.6, this.smoothScroll, this.scroll);
        class_4587 matrices = MatrixStackCompat.from(context.method_51448());
        rectangle.render(ShapeProperties.create(matrices, this.dropdownListX, this.dropDownListY, this.dropDownListWidth, this.dropDownListHeight).round(6.0f).thickness(1.15f).softness(1.2f).outlineColor(ColorUtil.multAlpha(-13418414, opacity * 0.74f)).color(ColorUtil.multAlpha(-15656926, opacity * 0.82f)).build());
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrices.method_23760().method_23761(), this.dropdownListX, this.dropDownListY + 1.0f, this.dropDownListWidth, this.dropDownListHeight - 2.0f);
        float offset = this.dropDownListY + (float)this.smoothScroll;
        for (SelectedButton button : this.selectedButtons) {
            button.x = this.dropdownListX;
            button.y = offset;
            button.width = this.dropDownListWidth;
            button.height = 18.0f;
            button.setAlpha(opacity);
            if (offset + 18.0f >= this.dropDownListY && offset <= this.dropDownListY + this.dropDownListHeight) {
                button.render(context, mouseX, mouseY, delta);
            }
            offset += 18.0f;
        }
        scissor.pop();
        if (contentHeight > this.dropDownListHeight) {
            float thumbH = class_3532.method_15363((float)(this.dropDownListHeight / contentHeight * this.dropDownListHeight), (float)18.0f, (float)this.dropDownListHeight);
            float progress = class_3532.method_15363((float)((float)(-this.smoothScroll / (double)Math.max(1.0f, maxScroll))), (float)0.0f, (float)1.0f);
            float thumbY = this.dropDownListY + (this.dropDownListHeight - thumbH) * progress;
            rectangle.render(ShapeProperties.create(matrices, this.dropdownListX + this.dropDownListWidth - 4.0f, this.dropDownListY + 4.0f, 2.0, this.dropDownListHeight - 8.0f).round(1.0f).color(ColorUtil.multAlpha(-14011838, opacity)).build());
            rectangle.render(ShapeProperties.create(matrices, this.dropdownListX + this.dropDownListWidth - 4.5f, thumbY + 2.0f, 3.0, thumbH - 4.0f).round(1.5f).color(ColorUtil.multAlpha(Hud.getInstance().colorSetting.getColor(), 0.72f * opacity)).build());
        }
    }

    private boolean isHoveredList(double mouseX, double mouseY) {
        return MathUtil.isHovered(mouseX, mouseY, this.dropdownListX, this.dropDownListY, this.dropDownListWidth, this.dropDownListHeight) || MathUtil.isHovered(mouseX, mouseY, this.pillX, this.pillY, this.pillWidth, 18.0);
    }
}

