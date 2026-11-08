/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1297
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_9848
 *  org.joml.Matrix3x2fStack
 *  org.joml.Vector3d
 */
package ru.recoin.common.util.math;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_9848;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3d;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.render.LegacyRenderSystem;

public final class MathUtil
implements QuickImports {
    public static double PI2 = Math.PI * 2;
    private static final float[] SIN_TABLE = new float[65536];

    public static float fastSin(float value) {
        return SIN_TABLE[(int)(value * 10430.378f) & 0xFFFF];
    }

    public static float fastCos(float value) {
        return SIN_TABLE[(int)(value * 10430.378f + 16384.0f) & 0xFFFF];
    }

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static double computeGcd() {
        double sensitivity = (Double)MathUtil.mc.field_1690.method_42495().method_41753();
        double multiplier = sensitivity * 0.6 + 0.2;
        boolean scoped = MathUtil.mc.field_1724 != null && !MathUtil.mc.field_1690.field_1914
                && MathUtil.mc.field_1690.method_31044().method_31034() && MathUtil.mc.field_1724.method_31550();
        return multiplier * multiplier * multiplier * (scoped ? 0.15 : 1.2);
    }

    public static int getRandom(int min, int max) {
        return (int)MathUtil.getRandom((float)min, (float)max + 1.0f);
    }

    public static float getRandom(float min, float max) {
        return (float)MathUtil.getRandom((double)min, (double)max);
    }

    public static double getRandom(double min, double max) {
        if (min == max) {
            return min;
        }
        if (min > max) {
            double d = min;
            min = max;
            max = d;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static void scale(class_4587 stack, float x, float y, float scale, Runnable data) {
        if (scale != 1.0f) {
            float scale2 = 0.5f + scale / 2.0f;
            stack.method_22903();
            stack.method_46416(x, y, 0.0f);
            stack.method_22905(scale2, scale2, 1.0f);
            stack.method_46416(-x, -y, 0.0f);
            MathUtil.setAlpha(scale, data);
            stack.method_22909();
        } else {
            data.run();
        }
    }

    public static void scale(Matrix3x2fStack stack, float x, float y, float scale, Runnable data) {
        if (scale != 1.0f) {
            float scale2 = 0.5f + scale / 2.0f;
            stack.pushMatrix();
            stack.translate(x, y);
            stack.scale(scale2, scale2);
            stack.translate(-x, -y);
            MathUtil.setAlpha(scale, data);
            stack.popMatrix();
        } else {
            data.run();
        }
    }

    public static void scale(class_4587 stack, float x, float y, float scaleX, float scaleY, Runnable data) {
        float sumScale = scaleX * scaleY;
        if (sumScale != 1.0f) {
            stack.method_22903();
            stack.method_46416(x, y, 0.0f);
            stack.method_22905(scaleX, scaleY, 1.0f);
            stack.method_46416(-x, -y, 0.0f);
            MathUtil.setAlpha(sumScale, data);
            stack.method_22909();
        } else {
            data.run();
        }
    }

    public static float blinking(double speed, float f) {
        float red = (float)((double)System.currentTimeMillis() % speed / (speed / (double)f));
        if (red > f / 2.0f) {
            red = f - red;
        }
        return red;
    }

    public static float textScrolling(float textWidth) {
        int speed = (int)(textWidth * 75.0f);
        return (float)class_3532.method_15350((double)((double)(System.currentTimeMillis() % (long)speed) * Math.PI / (double)speed), (double)0.0, (double)1.0) * textWidth;
    }

    public static void setAlpha(float alpha, Runnable data) {
        MathUtil.setColor(1.0f, 1.0f, 1.0f, alpha, data);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setColor(float red, float green, float blue, float alpha, Runnable data) {
        float[] color = LegacyRenderSystem.getShaderColor();
        float prevR = color[0];
        float prevG = color[1];
        float prevB = color[2];
        float prevA = color[3];
        LegacyRenderSystem.setShaderColor(class_3532.method_15363((float)red, (float)0.0f, (float)1.0f), class_3532.method_15363((float)green, (float)0.0f, (float)1.0f), class_3532.method_15363((float)blue, (float)0.0f, (float)1.0f), class_3532.method_15363((float)alpha, (float)0.0f, (float)1.0f));
        try {
            data.run();
        }
        finally {
            LegacyRenderSystem.setShaderColor(prevR, prevG, prevB, prevA);
        }
    }

    public static double round(double num, double increment) {
        double rounded = (double)Math.round(num / increment) * increment;
        return (double)Math.round(rounded * 100.0) / 100.0;
    }

    public static int floorNearestMulN(int x, int n) {
        return n * (int)Math.floor((double)x / (double)n);
    }

    public static int getRed(int hex) {
        return hex >> 16 & 0xFF;
    }

    public static int getGreen(int hex) {
        return hex >> 8 & 0xFF;
    }

    public static int getBlue(int hex) {
        return hex & 0xFF;
    }

    public static int getAlpha(int hex) {
        return hex >> 24 & 0xFF;
    }

    public static int applyOpacity(int color, float opacity) {
        return class_9848.method_61324((int)((int)((float)MathUtil.getAlpha(color) * opacity / 255.0f)), (int)MathUtil.getRed(color), (int)MathUtil.getGreen(color), (int)MathUtil.getBlue(color));
    }

    public static class_243 cosSin(int i, int size, double width) {
        int index = Math.min(i, size);
        float cos = (float)(Math.cos((double)index * PI2 / (double)size) * width);
        float sin = (float)(-Math.sin((double)index * PI2 / (double)size) * width);
        return new class_243((double)cos, 0.0, (double)sin);
    }

    public static double absSinAnimation(double input) {
        return Math.abs(1.0 + Math.sin(input)) / 2.0;
    }

    public static Vector3d interpolate(Vector3d prevPos, Vector3d pos) {
        float progress = tickCounter.getTickProgress(false);
        return new Vector3d(class_3532.method_16436((double)progress, (double)prevPos.x, (double)pos.x), class_3532.method_16436((double)progress, (double)prevPos.y, (double)pos.y), class_3532.method_16436((double)progress, (double)prevPos.z, (double)pos.z));
    }

    public static class_243 interpolate(class_243 prevPos, class_243 pos) {
        float progress = tickCounter.getTickProgress(false);
        return new class_243(class_3532.method_16436((double)progress, (double)prevPos.field_1352, (double)pos.field_1352), class_3532.method_16436((double)progress, (double)prevPos.field_1351, (double)pos.field_1351), class_3532.method_16436((double)progress, (double)prevPos.field_1350, (double)pos.field_1350));
    }

    public static class_243 interpolate(class_1297 entity) {
        if (entity == null) {
            return class_243.field_1353;
        }
        float progress = tickCounter.getTickProgress(false);
        return new class_243(class_3532.method_16436((double)progress, (double)entity.field_6014, (double)entity.method_23317()), class_3532.method_16436((double)progress, (double)entity.field_6036, (double)entity.method_23318()), class_3532.method_16436((double)progress, (double)entity.field_5969, (double)entity.method_23321()));
    }

    public static float interpolate(float prev, float orig) {
        return class_3532.method_16439((float)tickCounter.getTickProgress(false), (float)prev, (float)orig);
    }

    public static double interpolate(double prev, double orig) {
        return class_3532.method_16436((double)tickCounter.getTickProgress(false), (double)prev, (double)orig);
    }

    public static int interpolateSmooth(double smooth, int prev, int orig) {
        return (int)class_3532.method_16436((double)((double)tickCounter.getDynamicDeltaTicks() / smooth), (double)prev, (double)orig);
    }

    public static float interpolateSmooth(double smooth, float prev, float orig) {
        return (float)class_3532.method_16436((double)((double)tickCounter.getDynamicDeltaTicks() / smooth), (double)prev, (double)orig);
    }

    public static double interpolateSmooth(double smooth, double prev, double orig) {
        return class_3532.method_16436((double)((double)tickCounter.getDynamicDeltaTicks() / smooth), (double)prev, (double)orig);
    }

    private MathUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        for (int i = 0; i < 65536; ++i) {
            MathUtil.SIN_TABLE[i] = (float)Math.sin((double)i * Math.PI * 2.0 / 65536.0);
        }
    }
}

