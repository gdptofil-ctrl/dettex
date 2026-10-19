package ru.recoin.implement.features.modules.render;

import java.util.Collection;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_327;
import net.minecraft.class_332;
import ru.recoin.common.QuickImports;

/**
 * Hud 2: новый HUD в стиле скриншота (тёмные скруглённые панели, акцент темы).
 * Только DrawContext, без шейдеров и блюра.
 *
 * Рисуется автоматически, когда в Hud выбран стиль "Hud 2". Имена под
 * intermediary-маппинги (net.minecraft.class_XXXX) 1.21.11.
 */
public final class NewHud implements QuickImports {

    // ---- Данные плеера: заполняются из MediaPlayer ----
    public static String mediaTitle = "";
    public static String mediaArtist = "";
    public static int mediaPos = 0;
    public static int mediaLen = 0;

    private NewHud() {
    }

    public static void render(class_332 ctx) {
        if (mc.field_1724 == null || mc.field_1690.field_1842) {
            return;
        }
        Hud hud = Hud.getInstance();
        if (hud == null) {
            return;
        }

        int accent = hud.themeColor();
        float opacity = hud.hudOpacitySetting.getValue();
        int bg = withAlpha(0x0B0B12, (int) (170.0f * opacity));

        if (hud.interfaceSettings.isSelected("Watermark")) {
            drawWatermark(ctx, hud.watermarkNameSetting.getText(), accent, bg);
        }
        if (hud.interfaceSettings.isSelected("Potions")) {
            drawPotions(ctx, accent, bg);
        }
        if (hud.interfaceSettings.isSelected("Armor")) {
            drawArmor(ctx, accent, bg);
        }
        if (hud.interfaceSettings.isSelected("Media Player")) {
            drawMedia(ctx, accent, bg);
        }
        drawCoords(ctx, bg);
    }

    // ---------------- Watermark (сверху по центру) ----------------
    private static void drawWatermark(class_332 ctx, String name, int accent, int bg) {
        class_327 tr = mc.field_1772;
        String fps = mc.method_47599() + " fps";
        String server = mc.method_1542() ? "local"
                : (mc.method_1558() != null ? mc.method_1558().field_3761 : "local");

        int nameW = tr.method_1727(name);
        int sepW = 7;
        int srvW = tr.method_1727(server);
        int fpsW = tr.method_1727(fps);
        int w = 10 + nameW + sepW + srvW + sepW + fpsW + 10;
        int h = 16;
        int x = (window.getScaledWidth() - w) / 2;
        int y = 4;

        roundRect(ctx, x, y, w, h, 5, bg);
        roundRect(ctx, x, y + h - 2, w, 2, 1, withAlpha(accent, 200));

        int tx = x + 10;
        int ty = y + 4;
        ctx.method_51433(tr, name, tx, ty, accent, true);
        tx += nameW;
        ctx.method_51433(tr, " | ", tx - 1, ty, 0x66FFFFFF, false);
        tx += sepW;
        ctx.method_51433(tr, server, tx, ty, 0xFFFFFFFF, true);
        tx += srvW;
        ctx.method_51433(tr, " | ", tx - 1, ty, 0x66FFFFFF, false);
        tx += sepW;
        ctx.method_51433(tr, fps, tx, ty, 0xFFDDDDDD, true);
    }

    // ---------------- Potions (слева) ----------------
    private static void drawPotions(class_332 ctx, int accent, int bg) {
        class_327 tr = mc.field_1772;
        Collection<class_1293> effects = mc.field_1724.method_6026();

        int x = 6;
        int y = 40;
        int w = 110;
        int rowH = 12;
        int h = 16 + Math.max(1, effects.size()) * rowH + 2;

        roundRect(ctx, x, y, w, h, 5, bg);
        ctx.method_51433(tr, "Potions", x + 6, y + 5, 0xFFFFFFFF, true);
        ctx.method_51433(tr, "\u2764", x + w - 12, y + 5, accent, false);
        ctx.method_25294(x + 6, y + 15, x + w - 6, y + 16, withAlpha(accent, 90));

        int ry = y + 19;
        for (class_1293 e : effects) {
            int effectColor = ((class_1291) e.method_5579().comp_349()).method_5556();
            String name = ((class_1291) e.method_5579().comp_349()).method_5560().getString() + " " + (e.method_5578() + 1);
            int sec = e.method_48559() ? -1 : e.method_5584() / 20;
            String time = sec < 0 ? "**" : (sec / 60) + ":" + String.format("%02d", sec % 60);
            int col = 0xFF000000 | effectColor;

            ctx.method_51433(tr, name, x + 6, ry, col, true);
            ctx.method_51433(tr, time, x + w - 6 - tr.method_1727(time), ry, 0xFFCCCCCC, true);
            ry += rowH;
        }
        if (effects.isEmpty()) {
            ctx.method_51433(tr, "\u041d\u0435\u0442 \u044d\u0444\u0444\u0435\u043a\u0442\u043e\u0432", x + 6, ry, 0x88FFFFFF, false);
        }
    }

    // ---------------- Armor (справа) ----------------
    private static void drawArmor(class_332 ctx, int accent, int bg) {
        class_1304[] slots = {class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
        int slot = 20;
        int w = slots.length * slot + 6;
        int h = 22;
        int x = window.getScaledWidth() - w - 6;
        int y = window.getScaledHeight() / 2 - h / 2;

        roundRect(ctx, x, y, w, h, 5, bg);
        for (int i = 0; i < slots.length; i++) {
            class_1799 st = mc.field_1724.method_6118(slots[i]);
            int ix = x + 3 + i * slot + 2;
            int iy = y + 3;
            if (!st.method_7960()) {
                ctx.method_51427(st, ix, iy);
                if (st.method_7963()) {
                    float f = 1.0f - (float) st.method_7919() / (float) st.method_7936();
                    int bw = Math.round(16.0f * f);
                    ctx.method_25294(ix, y + h - 3, ix + 16, y + h - 2, 0x66000000);
                    ctx.method_25294(ix, y + h - 3, ix + bw, y + h - 2, withAlpha(lerpColor(0xFF4444, accent, f), 255));
                }
            }
        }
    }

    // ---------------- Media player (снизу по центру) ----------------
    private static void drawMedia(class_332 ctx, int accent, int bg) {
        if (mediaTitle.isEmpty()) {
            return;
        }
        class_327 tr = mc.field_1772;

        int w = 135;
        int h = 32;
        int x = (window.getScaledWidth() - w) / 2;
        int y = window.getScaledHeight() - 80;

        roundRect(ctx, x, y, w, h, 6, bg);
        roundRect(ctx, x + 4, y + 4, 24, 24, 4, withAlpha(accent, 150));

        ctx.method_51433(tr, trim(tr, mediaTitle, w - 38), x + 33, y + 5, accent, true);
        ctx.method_51433(tr, trim(tr, mediaArtist, w - 38), x + 33, y + 15, 0xFFAAAAAA, false);

        String t = fmt(mediaPos) + " / " + fmt(mediaLen);
        ctx.method_51433(tr, t, x + 33, y + 23, 0xFFDDDDDD, false);

        if (mediaLen > 0) {
            int bx = x + 4;
            int bw = w - 8;
            ctx.method_25294(bx, y + h - 2, bx + bw, y + h - 1, 0x44FFFFFF);
            ctx.method_25294(bx, y + h - 2, bx + (int) (bw * Math.min(1.0f, (float) mediaPos / (float) mediaLen)), y + h - 1, accent);
        }
    }

    // ---------------- Координаты (слева снизу) ----------------
    private static void drawCoords(class_332 ctx, int bg) {
        class_327 tr = mc.field_1772;
        String c = String.format("XYZ: %d %d %d", (int) mc.field_1724.method_23317(), (int) mc.field_1724.method_23318(), (int) mc.field_1724.method_23321());
        int w = tr.method_1727(c) + 12;
        int x = 6;
        int y = window.getScaledHeight() - 22;
        roundRect(ctx, x, y, w, 14, 4, bg);
        ctx.method_51433(tr, c, x + 6, y + 3, 0xFFFFFFFF, true);
    }

    // ---------------- Утилиты ----------------
    public static void roundRect(class_332 ctx, int x, int y, int w, int h, int r, int color) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            ctx.method_25294(x, y, x + w, y + h, color);
            return;
        }
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            ctx.method_25294(x + inset, y + i, x + w - inset, y + i + 1, color);
            ctx.method_25294(x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
        }
        ctx.method_25294(x, y + r, x + w, y + h - r, color);
    }

    private static int withAlpha(int rgb, int a) {
        return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0xFFFFFF);
    }

    private static int lerpColor(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static String fmt(int s) {
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }

    private static String trim(class_327 tr, String s, int maxW) {
        if (tr.method_1727(s) <= maxW) {
            return s;
        }
        while (s.length() > 1 && tr.method_1727(s + "...") > maxW) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "...";
    }
}
