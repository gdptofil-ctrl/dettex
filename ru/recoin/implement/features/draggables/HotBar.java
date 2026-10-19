/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1306
 *  net.minecraft.class_1661
 *  net.minecraft.class_1799
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.draggables;

import java.util.Objects;
import net.minecraft.class_1306;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.ItemStackFade;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.modules.render.CooldownHotbar;

public class HotBar
extends AbstractDraggable {
    private final ItemStackFade[] stackFades = new ItemStackFade[10];
    private float selectItemX;

    public HotBar() {
        super("HotBar", 0, 50, 182, 22, false);
        for (int i = 0; i < this.stackFades.length; ++i) {
            this.stackFades[i] = new ItemStackFade();
        }
    }

    @Override
    public void drawDraggable(class_332 context) {
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        class_1661 inventory = Objects.requireNonNull(HotBar.mc.field_1724).method_31548();
        class_1799 offHand = HotBar.mc.field_1724.method_6079();
        this.selectItemX = MathUtil.interpolateSmooth(1.0, this.selectItemX, (float)(inventory.method_67532() * 20));
        this.setX((mc.method_22683().method_4486() - this.getWidth()) / 2);
        this.setY(mc.method_22683().method_4502() - 27);
        blur.render(ShapeProperties.create(matrix, (float)this.getX() - 0.5f, (float)this.getY() - 0.5f, this.getWidth() + 1, 23.0).round(5.0f).thickness(0.0f).softness(5.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
        rectangle.render(ShapeProperties.create(matrix, (float)this.getX() + this.selectItemX + 1.0f, this.getY() + 1, 20.0, 20.0).round(5.0f).thickness(3.0f).outlineColor(ColorUtil.getClientColor()).color(ColorUtil.getRect(0.0f)).build());
        for (int i = 0; i < 9; ++i) {
            this.drawStack(matrix, context, (class_1799)inventory.method_67533().get(i), this.getX() + i * 20 + 2, this.getY() + 2, false, i);
        }
        this.drawStack(matrix, context, offHand, this.getX() + (Objects.requireNonNull(HotBar.mc.field_1724).method_6068().equals((Object)class_1306.field_6183) ? -28 : 198), this.getY() + 2, true, 9);
    }

    public void drawStack(class_4587 matrix, class_332 context, class_1799 stack, float x, float y, boolean offHand, int slot) {
        if (offHand && !stack.method_7960()) {
            blur.render(ShapeProperties.create(matrix, x - 2.5f, y - 2.5f, 23.0, 23.0).round(5.0f).thickness(0.0f).softness(5.0f).quality(50.0f).outlineColor(0).color(ColorUtil.DARK_BLACK).build());
        }
        this.stackFades[slot].draw(context, stack, x, y, false, true, 1.0f);
        CooldownHotbar cooldownHotbar = CooldownHotbar.getInstance();
        if (cooldownHotbar != null && cooldownHotbar.isState()) {
            cooldownHotbar.renderCooldownText(context, stack, x - 2.0f, y);
        }
    }
}

