/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 */
package ru.recoin.common.util.render;

import net.minecraft.class_4587;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.Render2DUtil;

public final class InventorySlotRenderer
implements QuickImports {
    private InventorySlotRenderer() {
    }

    public static void background(class_4587 matrix, float x, float y) {
        rectangle.render(ShapeProperties.create(matrix, x, y, 9.0, 9.0).round(3.0f).color(ColorUtil.getOutline(0.25f)).build());
    }

    public static void cross(class_4587 matrix, float x, float y, float alpha) {
        FontRenderer font = Fonts.getSize(12, Fonts.Type.COMBINED_ICONS);
        String glyph = "\ue0c3";
        FontRenderer.TextBounds bounds = font.getStringBounds(glyph);
        font.drawString(matrix, glyph, x + (9.0f - bounds.width()) * 0.5f - bounds.x(), y + (9.0f - bounds.height()) * 0.5f - bounds.y() - Render2DUtil.getTextYOffset(), ColorUtil.multAlpha(-8748656, alpha * 0.59f));
    }
}

