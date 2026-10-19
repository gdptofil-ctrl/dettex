/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.chars.Char2IntArrayMap
 *  net.minecraft.class_3532
 *  org.joml.Vector4i
 */
package ru.recoin.common.util.color;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import java.awt.Color;
import java.util.regex.Pattern;
import net.minecraft.class_3532;
import org.joml.Vector4i;
import ru.recoin.implement.features.modules.render.Hud;

public final class ColorUtil {
    public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00a7[0-9a-f-or]");
    public static Char2IntArrayMap colorCodes = new Char2IntArrayMap(){
        {
            this.put('0', 0);
            this.put('1', 170);
            this.put('2', 43520);
            this.put('3', 43690);
            this.put('4', 0xAA0000);
            this.put('5', 0xAA00AA);
            this.put('6', 0xFFAA00);
            this.put('7', 0xAAAAAA);
            this.put('8', 0x555555);
            this.put('9', 0x5555FF);
            this.put('A', 0x55FF55);
            this.put('B', 0x55FFFF);
            this.put('C', 0xFF5555);
            this.put('D', 0xFF55FF);
            this.put('E', 0xFFFF55);
            this.put('F', 0xFFFFFF);
        }
    };
    public static final int RED = ColorUtil.getColor(255, 0, 0);
    public static final int GREEN = ColorUtil.getColor(0, 255, 0);
    public static final int BLUE = ColorUtil.getColor(0, 0, 255);
    public static final int YELLOW = ColorUtil.getColor(255, 255, 0);
    public static final int WHITE = ColorUtil.getColor(255);
    public static final int BLACK = ColorUtil.getColor(0);
    public static final int HALF_BLACK = ColorUtil.getColor(0, 0.5f);
    public static final int DARK_BLACK = ColorUtil.getColor(0, 0.8f);
    public static final int LIGHT_RED = ColorUtil.getColor(255, 85, 85);

    public static int red(int c) {
        return c >> 16 & 0xFF;
    }

    public static int green(int c) {
        return c >> 8 & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    public static int alpha(int c) {
        return c >> 24 & 0xFF;
    }

    public static float redf(int c) {
        return (float)ColorUtil.red(c) / 255.0f;
    }

    public static float greenf(int c) {
        return (float)ColorUtil.green(c) / 255.0f;
    }

    public static float bluef(int c) {
        return (float)ColorUtil.blue(c) / 255.0f;
    }

    public static float alphaf(int c) {
        return (float)ColorUtil.alpha(c) / 255.0f;
    }

    public static int[] getRGBA(int c) {
        return new int[]{ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c), ColorUtil.alpha(c)};
    }

    public static int[] getRGB(int c) {
        return new int[]{ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c)};
    }

    public static float[] getRGBAf(int c) {
        return new float[]{ColorUtil.redf(c), ColorUtil.greenf(c), ColorUtil.bluef(c), ColorUtil.alphaf(c)};
    }

    public static float[] getRGBf(int c) {
        return new float[]{ColorUtil.redf(c), ColorUtil.greenf(c), ColorUtil.bluef(c)};
    }

    public static int getColor(float red, float green, float blue, float alpha) {
        return ColorUtil.getColor(Math.round(red * 255.0f), Math.round(green * 255.0f), Math.round(blue * 255.0f), Math.round(alpha * 255.0f));
    }

    public static int getColor(int red, int green, int blue, float alpha) {
        return ColorUtil.getColor(red, green, blue, Math.round(alpha * 255.0f));
    }

    public static int getColor(float red, float green, float blue) {
        return ColorUtil.getColor(red, green, blue, 1.0f);
    }

    public static int getColor(int brightness, int alpha) {
        return ColorUtil.getColor(brightness, brightness, brightness, alpha);
    }

    public static int getColor(int brightness, float alpha) {
        return ColorUtil.getColor(brightness, Math.round(alpha * 255.0f));
    }

    public static int getColor(int brightness) {
        return ColorUtil.getColor(brightness, brightness, brightness);
    }

    public static int replAlpha(int color, int alpha) {
        return color & 0xFFFFFF | Math.max(0, Math.min(255, alpha)) << 24;
    }

    public static int replAlpha(int color, float alpha) {
        return color & 0xFFFFFF | Math.max(0, Math.min(255, Math.round(alpha * 255.0f))) << 24;
    }

    public static int multAlpha(int color, float percent01) {
        if (percent01 >= 1.0f) {
            return color;
        }
        if (percent01 <= 0.0f) {
            return color & 0xFFFFFF;
        }
        int currentAlpha = color >>> 24 & 0xFF;
        int newAlpha = Math.min(255, Math.max(0, Math.round((float)currentAlpha * percent01)));
        return color & 0xFFFFFF | newAlpha << 24;
    }

    public static Vector4i multAlpha(Vector4i color, float percent01) {
        if (percent01 >= 1.0f) {
            return color;
        }
        return new Vector4i(ColorUtil.multAlpha(color.x, percent01), ColorUtil.multAlpha(color.y, percent01), ColorUtil.multAlpha(color.z, percent01), ColorUtil.multAlpha(color.w, percent01));
    }

    public static int multColor(int colorStart, int colorEnd, float progress) {
        return ColorUtil.getColor(Math.round((float)ColorUtil.red(colorStart) * (ColorUtil.redf(colorEnd) * progress)), Math.round((float)ColorUtil.green(colorStart) * (ColorUtil.greenf(colorEnd) * progress)), Math.round((float)ColorUtil.blue(colorStart) * (ColorUtil.bluef(colorEnd) * progress)), Math.round((float)ColorUtil.alpha(colorStart) * (ColorUtil.alphaf(colorEnd) * progress)));
    }

    public static int multRed(int colorStart, int colorEnd, float progress) {
        return ColorUtil.getColor(Math.round((float)ColorUtil.red(colorStart) * (ColorUtil.redf(colorEnd) * progress)), Math.round((float)ColorUtil.green(colorStart) * (ColorUtil.greenf(colorEnd) * progress)), Math.round((float)ColorUtil.blue(colorStart) * (ColorUtil.bluef(colorEnd) * progress)), Math.round((float)ColorUtil.alpha(colorStart) * (ColorUtil.alphaf(colorEnd) * progress)));
    }

    public static Vector4i multDark(Vector4i color, float percent01) {
        return new Vector4i(ColorUtil.multDark(color.x, percent01), ColorUtil.multDark(color.y, percent01), ColorUtil.multDark(color.z, percent01), ColorUtil.multDark(color.w, percent01));
    }

    public static int multDark(int color, float percent01) {
        return ColorUtil.getColor(Math.round((float)ColorUtil.red(color) * percent01), Math.round((float)ColorUtil.green(color) * percent01), Math.round((float)ColorUtil.blue(color) * percent01), ColorUtil.alpha(color));
    }

    public static int multBright(int color, float percent01) {
        return ColorUtil.getColor(Math.min(255, Math.round((float)ColorUtil.red(color) / percent01)), Math.min(255, Math.round((float)ColorUtil.green(color) / percent01)), Math.min(255, Math.round((float)ColorUtil.blue(color) / percent01)), ColorUtil.alpha(color));
    }

    public static int overCol(int color1, int color2, float percent01) {
        float percent = class_3532.method_15363((float)percent01, (float)0.0f, (float)1.0f);
        return ColorUtil.getColor(class_3532.method_48781((float)percent, (int)ColorUtil.red(color1), (int)ColorUtil.red(color2)), class_3532.method_48781((float)percent, (int)ColorUtil.green(color1), (int)ColorUtil.green(color2)), class_3532.method_48781((float)percent, (int)ColorUtil.blue(color1), (int)ColorUtil.blue(color2)), class_3532.method_48781((float)percent, (int)ColorUtil.alpha(color1), (int)ColorUtil.alpha(color2)));
    }

    public static Vector4i multRedAndAlpha(Vector4i color, float red, float alpha) {
        return new Vector4i(ColorUtil.multRedAndAlpha(color.x, red, alpha), ColorUtil.multRedAndAlpha(color.y, red, alpha), ColorUtil.multRedAndAlpha(color.w, red, alpha), ColorUtil.multRedAndAlpha(color.z, red, alpha));
    }

    public static int multRedAndAlpha(int color, float red, float alpha) {
        return ColorUtil.getColor(ColorUtil.red(color), Math.min(255, Math.round((float)ColorUtil.green(color) / red)), Math.min(255, Math.round((float)ColorUtil.blue(color) / red)), Math.round((float)ColorUtil.alpha(color) * alpha));
    }

    public static int multRed(int color, float percent01) {
        return ColorUtil.getColor(ColorUtil.red(color), Math.min(255, Math.round((float)ColorUtil.green(color) / percent01)), Math.min(255, Math.round((float)ColorUtil.blue(color) / percent01)), ColorUtil.alpha(color));
    }

    public static int multGreen(int color, float percent01) {
        return ColorUtil.getColor(Math.min(255, Math.round((float)ColorUtil.green(color) / percent01)), ColorUtil.green(color), Math.min(255, Math.round((float)ColorUtil.blue(color) / percent01)), ColorUtil.alpha(color));
    }

    public static int[] genGradientForText(int color1, int color2, int length) {
        int[] gradient = new int[length];
        for (int i = 0; i < length; ++i) {
            float pc = (float)i / (float)(length - 1);
            gradient[i] = ColorUtil.overCol(color1, color2, pc);
        }
        return gradient;
    }

    public static int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        float hue = (float)angle / 360.0f;
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        return ColorUtil.getColor(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), Math.round(opacity * 255.0f));
    }

    public static int fade(int speed, int index, int first, int second) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = angle >= 180 ? 360 - angle : angle;
        return ColorUtil.overCol(first, second, (float)angle / 180.0f);
    }

    public static int fade(int index) {
        Hud hud = Hud.getInstance();
        return hud.themeFadeColor(8, index);
    }

    public static Vector4i horizontalGradient(int start, int end, float alpha) {
        int left = ColorUtil.multAlpha(start, alpha);
        int right = ColorUtil.multAlpha(end, alpha);
        return new Vector4i(left, left, right, right);
    }

    public static Vector4i roundClientColor(float alpha) {
        return new Vector4i(ColorUtil.multAlpha(ColorUtil.fade(270), alpha), ColorUtil.multAlpha(ColorUtil.fade(0), alpha), ColorUtil.multAlpha(ColorUtil.fade(180), alpha), ColorUtil.multAlpha(ColorUtil.fade(90), alpha));
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        return ColorUtil.computeColor(red, green, blue, alpha);
    }

    public static int getColor(int red, int green, int blue) {
        return ColorUtil.getColor(red, green, blue, 255);
    }

    private static int computeColor(int red, int green, int blue, int alpha) {
        return class_3532.method_15340((int)alpha, (int)0, (int)255) << 24 | class_3532.method_15340((int)red, (int)0, (int)255) << 16 | class_3532.method_15340((int)green, (int)0, (int)255) << 8 | class_3532.method_15340((int)blue, (int)0, (int)255);
    }

    public static String formatting(int color) {
        return "\u23cf" + color + "\u23cf";
    }

    public static String removeFormatting(String text) {
        return text == null || text.isEmpty() ? null : FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    public static int getMainGuiColor() {
        return ColorUtil.getColor(10, 12, 18);
    }

    public static int getGuiRectColor(float alpha) {
        return ColorUtil.getColor(10, 12, 18);
    }

    public static int getGuiRectColor2(float alpha) {
        return ColorUtil.getColor(10, 12, 18);
    }

    public static int getRect(float alpha) {
        return ColorUtil.getColor(10, 12, 18);
    }

    public static int getRectDarker(float alpha) {
        return ColorUtil.getColor(10, 12, 18);
    }

    public static int getText(float alpha) {
        return ColorUtil.multAlpha(ColorUtil.getText(), alpha);
    }

    public static int getText() {
        return -1644826;
    }

    public static int getClientColor() {
        return Hud.getInstance().colorSetting.getColor();
    }

    public static int getClientColor(float alpha) {
        return ColorUtil.multAlpha(ColorUtil.getClientColor(), alpha);
    }

    public static int getFriendColor() {
        return -11141291;
    }

    public static int getOutline(float alpha, float bright) {
        return ColorUtil.multBright(ColorUtil.multAlpha(ColorUtil.getOutline(), alpha), bright);
    }

    public static int getOutline(float alpha) {
        return ColorUtil.multAlpha(ColorUtil.getOutline(), alpha);
    }

    public static int getOutline() {
        return -13158586;
    }

    private ColorUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

