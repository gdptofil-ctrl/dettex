/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587
 */
package ru.recoin.implement.screens.menu;

import net.minecraft.class_4587;
import ru.recoin.api.system.font.FontRenderer;
import ru.recoin.api.system.font.Fonts;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.Render2DUtil;
import ru.recoin.common.util.render.ScissorManager;
import ru.recoin.core.Main;
import ru.recoin.implement.screens.menu.MenuStyle;

public final class MenuControls {
    private MenuControls() {
    }

    public static float sliderProgress(float current, float target) {
        return MathUtil.interpolateSmooth(2.2, current, Math.max(0.0f, Math.min(1.0f, target)));
    }

    public static void toggle(class_4587 matrix, float x, float y, float progress) {
        float active = Math.max(0.0f, Math.min(1.0f, progress));
        MenuStyle.fill(matrix, x, y, 23.0f, 12.0f, 6.0f, ColorUtil.overCol(-16250094, MenuStyle.accent(), active));
        MenuStyle.fill(matrix, x + 1.5f + active * 11.0f, y + 1.5f, 9.0f, 9.0f, 4.5f, ColorUtil.overCol(-8419182, -1576965, active));
    }

    public static void slider(class_4587 matrix, float x, float centerY, float width, float progress) {
        float value = Math.max(0.0f, Math.min(1.0f, progress));
        float span = Math.max(0.0f, width - 8.0f);
        MenuStyle.fill(matrix, x + 4.0f, centerY - 1.5f, span, 3.0f, 1.5f, MenuStyle.cardHover());
        MenuStyle.fill(matrix, x + 4.0f, centerY - 1.5f, span * value, 3.0f, 1.5f, MenuStyle.accent());
        MenuStyle.fill(matrix, x + span * value, centerY - 4.0f, 8.0f, 8.0f, 4.0f, -986118);
    }

    public static void color(class_4587 matrix, float x, float y, int color, float bounce) {
        float size = 18.0f * bounce;
        float inset = (18.0f - size) / 2.0f;
        MenuStyle.fill(matrix, x + inset, y + inset, size, size, 4.0f * bounce, color);
        MenuStyle.outline(matrix, x + inset, y + inset, size, size, 4.0f * bounce, 0.65f, -2138005579);
    }

    public static float bindWidth(String text) {
        return MenuStyle.font(7.5f).getStringBounds(text).width() + Fonts.getMenuSize(12, Fonts.Type.COMBINED_ICONS).getStringBounds("\ue01d").width() + 11.0f;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void bind(class_4587 matrix, float right, float centerY, float width, String text, float active) {
        float x = right - width;
        MenuStyle.fill(matrix, x, centerY - 6.75f, width, 13.5f, 3.5f, ColorUtil.overCol(1109666616, ColorUtil.multAlpha(MenuStyle.accent(), 0.35f), active));
        ScissorManager scissor = Main.getInstance().getScissorManager();
        scissor.push(matrix.method_23760().method_23761(), x + 2.0f, centerY - 6.75f, Math.max(0.0f, width - 4.0f), 13.5f);
        try {
            FontRenderer icon = Fonts.getMenuSize(12, Fonts.Type.COMBINED_ICONS);
            float iconWidth = icon.getStringBounds("\ue01d").width();
            MenuControls.centered(matrix, icon, "\ue01d", x + 4.0f + iconWidth / 2.0f, centerY, MenuStyle.accent());
            MenuControls.centered(matrix, MenuStyle.font(7.5f), text, (x + 4.0f + iconWidth + 3.0f + right - 4.0f) / 2.0f, centerY, ColorUtil.overCol(-7301470, -986118, active));
        }
        finally {
            scissor.pop();
        }
    }

    private static void centered(class_4587 matrix, FontRenderer font, String text, float x, float y, int color) {
        FontRenderer.TextBounds bounds = font.getStringBounds(text);
        font.drawString(matrix, text, x - bounds.x() - bounds.width() / 2.0f, y - bounds.y() - bounds.height() / 2.0f - Render2DUtil.getTextYOffset(), color);
    }
}

