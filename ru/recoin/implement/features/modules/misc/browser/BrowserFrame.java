package ru.recoin.implement.features.modules.misc.browser;

import net.minecraft.class_243;
import net.minecraft.class_332;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.implement.features.modules.render.Hud;

/**
 * Frame around the Browser window, on screen and in the world. Styles: Theme (client colour),
 * Winter (icy gradient with falling snowflakes), Halloween (orange-purple with pumpkins),
 * Rainbow, Neon (pulsing glow) and Custom (two colours of your choice).
 */
public final class BrowserFrame {
    public record Style(String name, int customA, int customB, boolean glow, boolean decorations) {
    }

    private BrowserFrame() {
    }

    private static double t() {
        return (System.currentTimeMillis() % 3_600_000L) / 1000.0;
    }

    static int hsv(float h, float s, float v) {
        return 0xFF000000 | java.awt.Color.HSBtoRGB(h, s, v) & 0xFFFFFF;
    }

    static int mix(int a, int b, float k) {
        k = Math.max(0.0f, Math.min(1.0f, k));
        int r = (int)((a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * k);
        int g = (int)((a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * k);
        int bl = (int)((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * k);
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    static int alpha(int c, float a) {
        return Math.round(Math.max(0.0f, Math.min(1.0f, a)) * 255) << 24 | c & 0xFFFFFF;
    }

    /** The two gradient colours of the frame right now (top, bottom). */
    public static int[] colors(Style s) {
        double t = BrowserFrame.t();
        return switch (s.name()) {
            case "Winter" -> new int[]{0xFFE8F7FF, 0xFF5AB6FF};
            case "Halloween" -> new int[]{0xFFFF8A1E, 0xFF8E3CFF};
            case "Rainbow" -> new int[]{BrowserFrame.hsv((float)(t * 0.12 % 1.0), 0.8f, 1.0f), BrowserFrame.hsv((float)((t * 0.12 + 0.35) % 1.0), 0.8f, 1.0f)};
            case "Neon" -> {
                float p = (float)(0.5 + 0.5 * Math.sin(t * 3.0));
                yield new int[]{BrowserFrame.mix(0xFF00F0FF, 0xFFFF2BD6, p), BrowserFrame.mix(0xFFFF2BD6, 0xFF00F0FF, p)};
            }
            case "Custom" -> new int[]{s.customA() | 0xFF000000, s.customB() | 0xFF000000};
            default -> {
                Hud hud = Hud.getInstance();
                int c = hud != null ? hud.themeColor() | 0xFF000000 : 0xFF4B8BFF;
                yield new int[]{c, BrowserFrame.mix(c, 0xFF101018, 0.45f)};
            }
        };
    }

    // ------------------------------------------------------------------ on screen

    /** Frame around the rectangle x..x+w, y..y+h (the page), drawn outside it. */
    public static void draw2D(class_332 c, int x, int y, int w, int h, int bar, Style s) {
        int[] col = BrowserFrame.colors(s);
        int b = 4;
        // soft glow
        if (s.glow()) {
            for (int i = 6; i >= 1; --i) {
                int a = BrowserFrame.alpha(col[0], 0.05f * (7 - i));
                c.method_25294(x - b - i, y - bar - b - i, x + w + b + i, y + h + b + i, a);
            }
        }
        // gradient frame (top colour -> bottom colour)
        c.method_25296(x - b, y - bar - b, x + w + b, y + h + b, col[0], col[1]);
        // dark title bar under the buttons
        c.method_25294(x, y - bar, x + w, y - 1, 0xEE16181F);
        if (!s.decorations()) {
            return;
        }
        double t = BrowserFrame.t();
        switch (s.name()) {
            case "Winter" -> {
                // snowflakes sliding along the frame + little drifts on the top edge
                for (int i = 0; i < 14; ++i) {
                    double f = (t * 0.06 + i / 14.0) % 1.0;
                    int sx = x - b + (int)(f * (w + 2 * b));
                    int sy = y - bar - b - 3 + (int)(Math.sin(t * 2 + i) * 2);
                    BrowserFrame.snowflake(c, sx, sy, i % 3 == 0 ? 3 : 2);
                }
                for (int i = 0; i < w; i += 9) {
                    int hgt = 2 + (int)(Math.abs(Math.sin(i * 0.37)) * 3);
                    c.method_25294(x + i, y - bar - b - hgt, x + i + 9, y - bar - b, 0xFFF4FBFF);
                }
            }
            case "Halloween" -> {
                BrowserFrame.pumpkin(c, x - b - 7, y - bar - b - 7, t);
                BrowserFrame.pumpkin(c, x + w + b - 7, y - bar - b - 7, t + 1.3);
                BrowserFrame.pumpkin(c, x - b - 7, y + h + b - 7, t + 2.1);
                BrowserFrame.pumpkin(c, x + w + b - 7, y + h + b - 7, t + 0.7);
                // a few bats flapping over the top edge
                for (int i = 0; i < 3; ++i) {
                    double f = (t * 0.08 + i / 3.0) % 1.0;
                    BrowserFrame.bat(c, x + (int)(f * w), y - bar - b - 12 - (int)(Math.sin(t * 3 + i) * 3), t * 10 + i);
                }
            }
            default -> {
            }
        }
    }

    private static void snowflake(class_332 c, int x, int y, int r) {
        int col = 0xFFFFFFFF;
        c.method_25294(x - r, y, x + r + 1, y + 1, col);
        c.method_25294(x, y - r, x + 1, y + r + 1, col);
        c.method_25294(x - r + 1, y - r + 1, x - r + 2, y - r + 2, col);
        c.method_25294(x + r - 1, y - r + 1, x + r, y - r + 2, col);
        c.method_25294(x - r + 1, y + r - 1, x - r + 2, y + r, col);
        c.method_25294(x + r - 1, y + r - 1, x + r, y + r, col);
    }

    private static void pumpkin(class_332 c, int x, int y, double t) {
        int body = 0xFFFF8A1E;
        int dark = 0xFFD06410;
        c.method_25294(x + 2, y + 3, x + 12, y + 13, body);
        c.method_25294(x + 1, y + 5, x + 13, y + 11, body);
        c.method_25294(x + 6, y + 3, x + 8, y + 13, dark);          // rib
        c.method_25294(x + 6, y, x + 8, y + 3, 0xFF3D8B37);        // stem
        int glow = (int)(180 + 75 * Math.sin(t * 6.0));
        int eye = glow << 24 | 0xFFE36A;
        c.method_25294(x + 3, y + 6, x + 5, y + 8, eye);
        c.method_25294(x + 9, y + 6, x + 11, y + 8, eye);
        c.method_25294(x + 4, y + 9, x + 10, y + 10, eye);
    }

    private static void bat(class_332 c, int x, int y, double phase) {
        int col = 0xFF2A1A3A;
        int flap = Math.sin(phase) > 0 ? -2 : 1;
        c.method_25294(x - 1, y, x + 2, y + 3, col);
        c.method_25294(x - 5, y + 1 + flap, x - 1, y + 2 + flap, col);
        c.method_25294(x + 2, y + 1 + flap, x + 6, y + 2 + flap, col);
    }

    // ------------------------------------------------------------------ in the world

    /**
     * Frame around a panel in the world. center: middle of the page, right / up: unit vectors along
     * the page, hw / hh: half size of the page in blocks.
     */
    public static void draw3D(class_243 center, class_243 right, class_243 up, double hw, double hh, double border, Style s) {
        int[] col = BrowserFrame.colors(s);
        double ow = hw + border;
        double oh = hh + border;
        // four bars (top, bottom, left, right) with the gradient going from top colour to bottom colour
        BrowserFrame.bar(center, right, up, -ow, hh, ow, oh, col[0], col[0]);
        BrowserFrame.bar(center, right, up, -ow, -oh, ow, -hh, col[1], col[1]);
        BrowserFrame.bar(center, right, up, -ow, -hh, -hw, hh, col[1], col[0]);
        BrowserFrame.bar(center, right, up, hw, -hh, ow, hh, col[1], col[0]);
        if (s.glow()) {
            // glowing outline just outside the frame
            double g = border * 0.6;
            class_243 a = BrowserFrame.p(center, right, up, -ow - g, oh + g);
            class_243 b = BrowserFrame.p(center, right, up, ow + g, oh + g);
            class_243 c = BrowserFrame.p(center, right, up, ow + g, -oh - g);
            class_243 d = BrowserFrame.p(center, right, up, -ow - g, -oh - g);
            int ga = BrowserFrame.alpha(col[0], 0.8f);
            int gb = BrowserFrame.alpha(col[1], 0.8f);
            Render3DUtil.drawAdditiveLineGradient(a, b, ga, ga, 3.0f, true);
            Render3DUtil.drawAdditiveLineGradient(b, c, ga, gb, 3.0f, true);
            Render3DUtil.drawAdditiveLineGradient(c, d, gb, gb, 3.0f, true);
            Render3DUtil.drawAdditiveLineGradient(d, a, gb, ga, 3.0f, true);
        }
        if (!s.decorations()) {
            return;
        }
        double t = BrowserFrame.t();
        if ("Winter".equals(s.name())) {
            // snowflakes drifting down in front of the frame edges
            for (int i = 0; i < 10; ++i) {
                double f = (t * 0.05 + i / 10.0) % 1.0;
                double sx = -ow + (i * 0.37 % 1.0) * 2 * ow;
                double sy = oh + 0.25 - f * (2 * oh + 0.5);
                double r = 0.05 + (i % 3) * 0.02;
                class_243 cc = BrowserFrame.p(center, right, up, sx + Math.sin(t + i) * 0.05, sy);
                int w = 0xE0FFFFFF;
                Render3DUtil.drawLine(cc.method_1019(right.method_1021(-r)), cc.method_1019(right.method_1021(r)), w, 1.5f, true);
                Render3DUtil.drawLine(cc.method_1019(up.method_1021(-r)), cc.method_1019(up.method_1021(r)), w, 1.5f, true);
            }
        } else if ("Halloween".equals(s.name())) {
            double ps = border * 1.6 + 0.08;
            for (double[] corner : new double[][]{{-ow, oh}, {ow, oh}, {-ow, -oh}, {ow, -oh}}) {
                BrowserFrame.bar(center, right, up, corner[0] - ps / 2, corner[1] - ps / 2, corner[0] + ps / 2, corner[1] + ps / 2, 0xFFFF8A1E, 0xFFD06410);
                BrowserFrame.bar(center, right, up, corner[0] - ps * 0.08, corner[1] + ps / 2, corner[0] + ps * 0.08, corner[1] + ps * 0.7, 0xFF3D8B37, 0xFF3D8B37);
            }
        }
    }

    private static class_243 p(class_243 c, class_243 right, class_243 up, double x, double y) {
        return c.method_1019(right.method_1021(x)).method_1019(up.method_1021(y));
    }

    private static void bar(class_243 c, class_243 right, class_243 up, double x0, double y0, double x1, double y1, int bottom, int top) {
        // a hair in front of the page so it is never hidden by it
        class_243 nudge = right.method_1036(up).method_1029().method_1021(-0.002);
        class_243 a = BrowserFrame.p(c, right, up, x0, y0).method_1019(nudge);
        class_243 b = BrowserFrame.p(c, right, up, x1, y0).method_1019(nudge);
        class_243 d = BrowserFrame.p(c, right, up, x1, y1).method_1019(nudge);
        class_243 e = BrowserFrame.p(c, right, up, x0, y1).method_1019(nudge);
        Render3DUtil.drawGradientQuad(a, b, d, e, bottom, bottom, top, top, true);
    }
}
