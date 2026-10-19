/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  net.minecraft.class_10868
 *  net.minecraft.class_289
 *  net.minecraft.class_2960
 *  net.minecraft.class_5944
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.class_10868;
import net.minecraft.class_289;
import net.minecraft.class_2960;
import net.minecraft.class_5944;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class LegacyRenderSystem {
    private static final float[] SHADER_COLOR = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    private static class_2960 shaderTexture;
    private static boolean additiveBlend;
    private static boolean depthTestEnabled;
    private static boolean depthWriteEnabled;
    private static int depthFunc;
    private static float lastFov;
    private static final Matrix4f PROJECTION_MATRIX;

    private LegacyRenderSystem() {
    }

    public static void enableBlend() {
        GlStateManager._enableBlend();
    }

    public static void disableBlend() {
        GlStateManager._disableBlend();
    }

    public static void defaultBlendFunc() {
        additiveBlend = false;
        GlStateManager._blendFuncSeparate((int)770, (int)771, (int)1, (int)771);
    }

    public static void blendFunc(int src, int dst) {
        additiveBlend = src == 770 && dst == 1;
        GlStateManager._blendFuncSeparate((int)src, (int)dst, (int)1, (int)771);
    }

    public static void blendFunc(Object src, Object dst) {
        LegacyRenderSystem.blendFunc(LegacyRenderSystem.toGlBlendFactor(src), LegacyRenderSystem.toGlBlendFactor(dst));
    }

    public static void blendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) {
        additiveBlend = srcRgb == 770 && dstRgb == 1;
        GlStateManager._blendFuncSeparate((int)srcRgb, (int)dstRgb, (int)srcAlpha, (int)dstAlpha);
    }

    public static void enableDepthTest() {
        depthTestEnabled = true;
        GlStateManager._enableDepthTest();
    }

    public static void disableDepthTest() {
        depthTestEnabled = false;
        GlStateManager._disableDepthTest();
    }

    public static void enableCull() {
        GlStateManager._enableCull();
    }

    public static void disableCull() {
        GlStateManager._disableCull();
    }

    public static void depthMask(boolean mask) {
        depthWriteEnabled = mask;
        GlStateManager._depthMask((boolean)mask);
    }

    public static void depthFunc(int function) {
        depthFunc = function;
        GlStateManager._depthFunc((int)function);
    }

    public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GlStateManager._colorMask((boolean)red, (boolean)green, (boolean)blue, (boolean)alpha);
    }

    public static void lineWidth(float width) {
    }

    public static void activeTexture(int texture) {
        GlStateManager._activeTexture((int)texture);
    }

    public static void bindTexture(int texture) {
        GlStateManager._bindTexture((int)texture);
    }

    public static void bindTexture(Object texture) {
        if (texture instanceof class_10868) {
            class_10868 glTexture = (class_10868)texture;
            LegacyRenderSystem.bindTexture(glTexture.method_68427());
        }
    }

    public static int getTextureId(Object texture) {
        if (texture instanceof class_10868) {
            class_10868 glTexture = (class_10868)texture;
            return glTexture.method_68427();
        }
        return 0;
    }

    public static void setShaderTexture(int slot, class_2960 texture) {
        shaderTexture = texture;
    }

    public static void setShaderTexture(int slot, int texture) {
        shaderTexture = null;
    }

    public static class_2960 getShaderTexture() {
        return shaderTexture;
    }

    public static boolean isAdditiveBlend() {
        return additiveBlend;
    }

    public static boolean isDepthTestEnabled() {
        return depthTestEnabled;
    }

    public static boolean isDepthWriteEnabled() {
        return depthWriteEnabled;
    }

    public static int getDepthFunc() {
        return depthFunc;
    }

    public static void setLastFov(float fov) {
        lastFov = fov;
    }

    public static float getLastFov() {
        return lastFov;
    }

    public static class_5944 setShader(Object ignored) {
        return class_5944.field_57864;
    }

    public static void setShaderColor(float red, float green, float blue, float alpha) {
        LegacyRenderSystem.SHADER_COLOR[0] = red;
        LegacyRenderSystem.SHADER_COLOR[1] = green;
        LegacyRenderSystem.SHADER_COLOR[2] = blue;
        LegacyRenderSystem.SHADER_COLOR[3] = alpha;
    }

    public static float[] getShaderColor() {
        return SHADER_COLOR;
    }

    public static Matrix4f getProjectionMatrix() {
        return PROJECTION_MATRIX;
    }

    public static class_289 renderThreadTesselator() {
        return class_289.method_1348();
    }

    public static void assertOnRenderThread() {
    }

    public static boolean isOnRenderThread() {
        return true;
    }

    public static void recordRenderCall(Runnable runnable) {
        runnable.run();
    }

    public static void clearColor(float red, float green, float blue, float alpha) {
    }

    public static void clear(int mask) {
    }

    public static void enableScissor(int x, int y, int width, int height) {
        GlStateManager._enableScissorTest();
        GL11.glScissor((int)x, (int)y, (int)width, (int)height);
    }

    public static void disableScissor() {
        GlStateManager._disableScissorTest();
    }

    private static int toGlBlendFactor(Object factor) {
        if (factor instanceof Number) {
            Number number = (Number)factor;
            return number.intValue();
        }
        String name = String.valueOf(factor);
        int dot = name.lastIndexOf(46);
        if (dot >= 0) {
            name = name.substring(dot + 1);
        }
        return switch (name) {
            case "ZERO" -> 0;
            case "ONE" -> 1;
            case "SRC_COLOR" -> 768;
            case "ONE_MINUS_SRC_COLOR" -> 769;
            case "DST_COLOR" -> 774;
            case "ONE_MINUS_DST_COLOR" -> 775;
            case "SRC_ALPHA" -> 770;
            case "ONE_MINUS_SRC_ALPHA" -> 771;
            case "DST_ALPHA" -> 772;
            case "ONE_MINUS_DST_ALPHA" -> 773;
            case "CONSTANT_COLOR" -> 32769;
            case "ONE_MINUS_CONSTANT_COLOR" -> 32770;
            case "CONSTANT_ALPHA" -> 32771;
            case "ONE_MINUS_CONSTANT_ALPHA" -> 32772;
            case "SRC_ALPHA_SATURATE" -> 776;
            default -> 1;
        };
    }

    static {
        depthWriteEnabled = true;
        depthFunc = 515;
        PROJECTION_MATRIX = new Matrix4f();
    }
}

