/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.draggables;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.entity.PlayerIntersectionUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.InventorySlotRenderer;
import ru.recoin.common.util.render.ItemStackFade;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.implement.features.draggables.HudIconRail;

public class Inventory
extends AbstractDraggable {
    private final List<class_1799> stacks = new ArrayList<class_1799>(27);
    private final ItemStackFade[] stackFades = new ItemStackFade[27];
    private final float[] crossAlpha = new float[27];
    private boolean hasItems;

    public Inventory() {
        super("Inventory", 390, 10, 121, 64, true);
        for (int i = 0; i < this.stackFades.length; ++i) {
            this.stackFades[i] = new ItemStackFade();
            this.crossAlpha[i] = 1.0f;
        }
    }

    @Override
    public boolean visible() {
        return this.hasItems || PlayerIntersectionUtil.isChat(Inventory.mc.field_1755);
    }

    @Override
    public void tick() {
        this.stacks.clear();
        this.hasItems = false;
        for (int i = 9; i < 36; ++i) {
            class_1799 stack = Inventory.mc.field_1724.field_7514.method_5438(i);
            this.stacks.add(stack);
            if (stack.method_7960()) continue;
            this.hasItems = true;
        }
    }

    @Override
    public void drawDraggable(class_332 context) {
        this.setWidth(121);
        this.setHeight(64);
        class_4587 matrix = MatrixStackCompat.from(context.method_51448());
        FontRenderer titleFont = Fonts.getSize(15, Fonts.Type.INTER_BOLD);
        FontRenderer iconFont = Fonts.getSize(16, Fonts.Type.COMBINED_ICONS);
        String titleIcon = "\ue0af";
        float headerX = HudIconRail.animatedX(this, this.getX());
        float gridY = (float)this.getY() + 21.0f;
        HudIconRail.drawPanel(matrix, blur, rectangle, headerX, this.getY(), this.getWidth(), 17.0f);
        HudIconRail.drawPanel(matrix, blur, rectangle, this.getX(), gridY, this.getWidth(), 43.0f);
        float iconWidth = iconFont.getStringBounds(titleIcon).width();
        HudIconRail.drawText(matrix, iconFont, titleIcon, headerX + 4.0f, (float)this.getY() + 4.0f, 9.0f, ColorUtil.getClientColor());
        HudIconRail.drawText(matrix, titleFont, this.getName(), headerX + 4.0f + iconWidth + 4.0f, (float)this.getY() + 4.0f, 9.0f, ColorUtil.getText());
        for (int i = 0; i < this.stacks.size(); ++i) {
            class_1799 stack = this.stacks.get(i);
            float slotX = (float)this.getX() + 4.0f + (float)(i % 9) * 13.0f;
            float slotY = gridY + 4.0f + (float)(i / 9) * 13.0f;
            InventorySlotRenderer.background(matrix, slotX, slotY);
            float crossTarget = stack.method_7960() ? 1.0f : 0.0f;
            this.crossAlpha[i] = MathUtil.interpolateSmooth(1.25, this.crossAlpha[i], crossTarget);
            if (this.crossAlpha[i] > 0.01f) {
                InventorySlotRenderer.cross(matrix, slotX, slotY, this.crossAlpha[i]);
            }
            this.stackFades[i].draw(context, stack, slotX + -0.5f, slotY + -0.5f, false, true, 0.5f);
        }
    }

    private boolean hasFadingItems() {
        for (ItemStackFade fade : this.stackFades) {
            if (!fade.isActive()) continue;
            return true;
        }
        return false;
    }
}

