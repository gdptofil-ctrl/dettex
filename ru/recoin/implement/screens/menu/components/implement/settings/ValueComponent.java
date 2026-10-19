/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu.components.implement.settings;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import ru.recoin.api.feature.module.setting.implement.ValueSetting;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.MenuDescriptionLocalizer;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.AbstractSettingComponent;

public class ValueComponent
extends AbstractSettingComponent {
    private static final double SLIDER_LERP_SMOOTH = 2.6;
    private static final float LABEL_X = 20.0f;
    private static final float EDGE_PAD = 8.0f;
    private static final float TRACK_HEIGHT = 2.5f;
    private static final float THUMB_DIAMETER = 8.0f;
    private final ValueSetting setting;
    private boolean dragging;
    private double animation;
    private float sliderX;
    private float sliderCenterY;
    private float sliderWidth;

    public ValueComponent(ValueSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        this.height = 38.0f;
        int accent = Hud.getInstance().colorSetting.getColor();
        this.sliderX = this.x + 20.0f;
        this.sliderWidth = Math.max(40.0f, this.x + this.width - 8.0f - this.sliderX);
        this.sliderCenterY = this.y + 31.0f;
        String value = StringUtil.ellipsize(String.valueOf(this.setting.getValue()), 40.0f, 11);
        float valueWidth = Fonts.getMenuSize(11, Fonts.Type.BOLD).getStringWidth(value);
        float valueX = this.x + this.width - 8.0f - valueWidth;
        float descWidth = Math.max(20.0f, valueX - (this.x + 20.0f) - 6.0f);
        String description = StringUtil.ellipsize(MenuDescriptionLocalizer.localize(this.setting.getDescription()), descWidth, 9);
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, DropdownMenuComponent.settingName(this.setting.getName()), this.x + 20.0f, this.y + 11.0f, -1446412);
        Fonts.getMenuSize(9).drawString(matrix, description, this.x + 20.0f, this.y + 21.0f, -10656136);
        Fonts.getMenuSize(11, Fonts.Type.BOLD).drawString(matrix, value, valueX, this.y + 11.0f, ColorUtil.multAlpha(accent, 0.95f));
        if (this.dragging) {
            this.applyDrag(mouseX);
        }
        this.drawSlider(matrix, accent);
    }

    private void drawSlider(class_4587 matrix, int accent) {
        float range = this.setting.getMax() - this.setting.getMin();
        float percent = range == 0.0f ? 0.0f : class_3532.method_15363((float)((this.setting.getValue() - this.setting.getMin()) / range), (float)0.0f, (float)1.0f);
        this.animation = MathUtil.interpolateSmooth(2.6, this.animation, (double)(this.sliderWidth * percent));
        float fillWidth = class_3532.method_15363((float)((float)this.animation), (float)0.0f, (float)this.sliderWidth);
        float trackY = this.sliderCenterY - 1.25f;
        rectangle.render(ShapeProperties.create(matrix, this.sliderX, trackY, this.sliderWidth, 2.5).round(1.25f).color(-14011838).build());
        rectangle.render(ShapeProperties.create(matrix, this.sliderX, trackY, fillWidth, 2.5).round(1.25f).color(ColorUtil.multBright(accent, 0.82f), accent, ColorUtil.multBright(accent, 0.82f), accent).build());
        float thumbX = this.sliderX + fillWidth;
        rectangle.render(ShapeProperties.create(matrix, thumbX - 5.0f, this.sliderCenterY - 5.0f, 10.0, 10.0).round(5.0f).color(ColorUtil.multAlpha(accent, 0.16f)).build());
        rectangle.render(ShapeProperties.create(matrix, thumbX - 4.0f, this.sliderCenterY - 4.0f, 8.0, 8.0).round(4.0f).color(-1).build());
    }

    private void applyDrag(double mouseX) {
        float value;
        float difference = class_3532.method_15363((float)((float)mouseX - this.sliderX), (float)0.0f, (float)this.sliderWidth);
        BigDecimal bd = BigDecimal.valueOf(difference / this.sliderWidth * (this.setting.getMax() - this.setting.getMin()) + this.setting.getMin()).setScale(2, RoundingMode.HALF_UP);
        float f = value = difference == 0.0f ? this.setting.getMin() : bd.floatValue();
        if (this.setting.isInteger()) {
            value = (int)value;
        }
        this.setting.setValue(value);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean hoveredSlider = MathUtil.isHovered(mouseX, mouseY, this.sliderX - 3.0f, this.sliderCenterY - 6.0f, this.sliderWidth + 6.0f, 12.0);
        if (hoveredSlider && button == 0) {
            this.dragging = true;
            this.applyDrag(mouseX);
        }
        if (hoveredSlider && button == 2) {
            this.setting.setValue(this.setting.getDefaultValue());
            this.dragging = false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}

