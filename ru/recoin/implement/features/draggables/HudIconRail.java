/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.features.draggables;

import net.minecraft.class_4587;
import ru.recoin.api.feature.draggable.AbstractDraggable;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.api.system.shape.implement.Arc;
import ru.recoin.api.system.shape.implement.Blur;
import ru.recoin.api.system.shape.implement.Rectangle;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.Render2DUtil;

public final class HudIconRail {
    static final float PADDING = 4.0f;
    static final float RAIL_WIDTH = 16.0f;
    static final float CONTENT_OFFSET = 20.0f;
    static final float CONTENT_TEXT_X = 24.0f;
    static final float HEADER_HEIGHT = 9.0f;
    static final float ROW_HEIGHT = 9.0f;
    static final float ROW_STEP = 11.0f;
    static final float FIRST_ROW_Y = 17.0f;

    private HudIconRail() {
    }

    static void draw(AbstractDraggable draggable, class_4587 matrix, Blur blur, Rectangle rectangle, float x, float y, float width, float height) {
        HudIconRail.drawPanel(matrix, blur, rectangle, HudIconRail.animatedX(draggable, x), y, 16.0f, height);
        HudIconRail.drawPanel(matrix, blur, rectangle, x + 20.0f, y, width - 20.0f, height);
    }

    public static void drawPanel(class_4587 matrix, Blur blur, Rectangle rectangle, float x, float y, float width, float height) {
        blur.render(ShapeProperties.create(matrix, x, y, width, height).round(6.0f).softness(3.5f).thickness(0.0f).quality(50.0f).outlineColor(0).color(-268435456).build());
        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(6.0f).softness(1.0f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.getOutline(0.8f)).color(0).build());
    }

    static int headerWidth(FontRenderer font, String title) {
        return Math.max(100, (int)Math.ceil(24.0f + font.getStringWidth(title) + 4.0f));
    }

    static int height(float rows) {
        return (int)Math.ceil(17.0f + rows * 11.0f + Math.min(1.0f, rows) * 2.0f);
    }

    static void drawHeader(AbstractDraggable draggable, class_4587 matrix, FontRenderer font, FontRenderer iconFont, String title, String icon) {
        HudIconRail.drawHeader(draggable, matrix, font, iconFont, title, icon, 0.0f);
    }

    static void drawHeader(AbstractDraggable draggable, class_4587 matrix, FontRenderer font, FontRenderer iconFont, String title, String icon, float textInset) {
        HudIconRail.drawText(matrix, iconFont, icon, HudIconRail.iconX(draggable, draggable.getX(), iconFont.getStringBounds(icon).width()), (float)draggable.getY() + 4.0f, 9.0f, ColorUtil.getClientColor());
        HudIconRail.drawText(matrix, font, title, (float)draggable.getX() + 24.0f + textInset, (float)draggable.getY() + 4.0f, 9.0f, ColorUtil.getText());
    }

    static float textY(FontRenderer font, String text, float top, float height) {
        FontRenderer.TextBounds bounds = font.getStringBounds(text);
        return top + (height - bounds.height()) * 0.5f - bounds.y() - Render2DUtil.getTextYOffset();
    }

    static void drawText(class_4587 matrix, FontRenderer font, String text, float x, float top, float height, int color) {
        font.drawString(matrix, text, x - font.getStringBounds(text).x(), HudIconRail.textY(font, text, top, height), color);
    }

    static float timerWidth(FontRenderer font, String duration) {
        return font.getStringBounds(duration).width() + 11.5f;
    }

    static void drawTimer(class_4587 matrix, Rectangle rectangle, Arc arc, FontRenderer font, String duration, float right, float rowTop, float progress, int color) {
        float width = HudIconRail.timerWidth(font, duration);
        float x = right - width;
        rectangle.render(ShapeProperties.create(matrix, x, rowTop, width, 9.0).round(3.0f).softness(0.5f).thickness(1.0f).quality(50.0f).outlineColor(ColorUtil.getOutline(0.8f)).color(ColorUtil.getOutline(0.25f)).build());
        float progressX = x + 2.0f;
        float progressY = rowTop + 1.75f;
        arc.render(ShapeProperties.create(matrix, progressX, progressY, 5.5, 5.5).start(0.0f).end(360.0f).thickness(0.6f).color(-14802133).build());
        arc.render(ShapeProperties.create(matrix, progressX, progressY, 5.5, 5.5).start(-90.0f).end(-90.0f + 360.0f * progress).thickness(0.6f).color(-6577231).build());
        HudIconRail.drawText(matrix, font, duration, x + 9.5f, rowTop, 9.0f, color);
    }

    static float animatedX(AbstractDraggable draggable, float x) {
        float progress = Math.max(0.0f, Math.min(1.0f, draggable.getScaleAnimation().getOutput().floatValue()));
        return x - (1.0f - progress) * 5.0f;
    }

    static float iconX(AbstractDraggable draggable, float x, float iconWidth) {
        return HudIconRail.animatedX(draggable, x + (16.0f - iconWidth) * 0.5f);
    }
}

