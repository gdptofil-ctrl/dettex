/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  org.joml.Vector4f
 */
package ru.recoin.implement.screens.menu.components.implement.settings.multiselect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import ru.recoin.api.feature.module.setting.implement.MultiSelectSetting;
import ru.recoin.api.system.animation.Animation;
import ru.recoin.api.system.animation.Direction;
import ru.recoin.api.system.animation.implement.DecelerateAnimation;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.other.StringUtil;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.screens.menu.components.AbstractComponent;
import ru.recoin.implement.screens.menu.components.implement.other.DropdownMenuComponent;
import ru.recoin.implement.screens.menu.components.implement.settings.select.SelectedButton;

public class MultiSelectedButton
extends AbstractComponent {
    private final MultiSelectSetting setting;
    private final String text;
    private final Vector4f round;
    private float alpha;
    private final Animation alphaAnimation = new DecelerateAnimation().setMs(160).setValue(0.5);

    public MultiSelectedButton(MultiSelectSetting setting, String text) {
        this.setting = setting;
        this.text = text;
        this.round = SelectedButton.getRound(setting.getList(), text);
        this.alphaAnimation.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void render(class_332 context, int mouseX, int mouseY, float delta) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        this.alphaAnimation.setDirection(this.setting.getSelected().contains(this.text) ? Direction.FORWARDS : Direction.BACKWARDS);
        float opacity = this.alphaAnimation.getOutput().floatValue();
        boolean hovered = MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height);
        int selectedOpacity = ColorUtil.multAlpha(Hud.getInstance().colorSetting.getColor(), opacity * this.alpha * 0.62f);
        if (hovered) {
            rectangle.render(ShapeProperties.create(matrix, this.x + 2.0f, this.y + 2.0f, this.width - 4.0f, this.height - 4.0f).round(4.0f).color(ColorUtil.multAlpha(-1, this.alpha * 0.055f)).build());
        }
        if (!this.alphaAnimation.isFinished(Direction.BACKWARDS)) {
            rectangle.render(ShapeProperties.create(matrix, this.x + 2.0f, this.y + 2.0f, this.width - 4.0f, this.height - 4.0f).round(4.0f).color(selectedOpacity).build());
        }
        String shown = DropdownMenuComponent.optionName(this.setting != null ? this.setting.getName() : null, this.text);
        Fonts.getMenuSize(10, Fonts.Type.BOLD).drawString(matrix, StringUtil.ellipsize(shown, this.width - 14.0f, 10), this.x + 8.0f, this.y + 7.5f, ColorUtil.multAlpha(-1906702, this.alpha));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) && button == 0) {
            ArrayList<String> selected = new ArrayList<String>(this.setting.getSelected());
            if (selected.contains(this.text)) {
                selected.remove(this.text);
            } else {
                selected.add(this.text);
                this.sortSelectedAccordingToList(selected, this.setting.getList());
            }
            this.setting.setSelected(selected);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void sortSelectedAccordingToList(List<String> selected, List<String> list) {
        selected.sort(Comparator.comparingInt(list::indexOf));
    }

    public MultiSelectedButton setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }
}

