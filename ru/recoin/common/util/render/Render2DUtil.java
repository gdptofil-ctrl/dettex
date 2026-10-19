/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1058
 *  net.minecraft.class_10799
 *  net.minecraft.class_1799
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_4587
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1058;
import net.minecraft.class_10799;
import net.minecraft.class_1799;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.recoin.api.system.shape.ShapeProperties;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.ItemGuiAlpha;
import ru.recoin.common.util.render.ItemRender;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.MatrixStackCompat;
import ru.recoin.common.util.render.ShapeShaderRenderer;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.ShaderProgramKeys;

public final class Render2DUtil
implements QuickImports {
    private static final List<Quad> QUAD = new ArrayList<Quad>();
    private static class_332 activeContext;
    private static float textYOffset;
    private static float renderAlpha;
    private static boolean hudDecorationActive;
    private static boolean guiDecorationActive;
    private static boolean queuedShapeActive;

    public static void setActiveContext(class_332 context) {
        activeContext = context;
    }

    public static class_332 getActiveContext() {
        return activeContext;
    }

    public static void setTextYOffset(float offset) {
        textYOffset = offset;
    }

    public static float getTextYOffset() {
        return textYOffset;
    }

    public static void setRenderAlpha(float alpha) {
        renderAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }

    public static float getRenderAlpha() {
        return renderAlpha;
    }

    public static int applyRenderAlpha(int color) {
        return ColorUtil.multAlpha(color, renderAlpha);
    }

    public static void setHudDecorationActive(boolean active) {
        hudDecorationActive = active;
    }

    public static boolean isHudDecorationActive() {
        return hudDecorationActive;
    }

    public static void setGuiDecorationActive(boolean active) {
        guiDecorationActive = active;
    }

    public static boolean isGuiDecorationActive() {
        return guiDecorationActive;
    }

    public static void setQueuedShapeActive(boolean active) {
        queuedShapeActive = active;
    }

    public static boolean isQueuedShapeActive() {
        return queuedShapeActive;
    }

    public static void onRender(class_332 context) {
        if (!QUAD.isEmpty()) {
            class_4587 matrix = MatrixStackCompat.from(context.method_51448());
            Matrix4f matrix4f = matrix.method_23760().method_23761();
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.defaultBlendFunc();
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            class_287 buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
            QUAD.forEach(quad -> drawEngine.quad(matrix4f, buffer, quad.x, quad.y, quad.width, quad.height, Render2DUtil.applyRenderAlpha(quad.color)));
            BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
            LegacyRenderSystem.disableBlend();
            QUAD.clear();
        }
    }

    public static void defaultDrawStack(class_332 context, class_1799 stack, float x, float y, boolean rect, boolean drawItemInSlot, float scale) {
        Render2DUtil.defaultDrawStack(context, stack, x, y, rect, drawItemInSlot, scale, 1.0f);
    }

    public static void defaultDrawStack(class_332 context, class_1799 stack, float x, float y, boolean rect, boolean drawItemInSlot, float scale, float alpha) {
        Render2DUtil.defaultDrawStack(context, stack, x, y, rect, drawItemInSlot, scale, alpha, 0);
    }

    public static void defaultDrawStack(class_332 context, class_1799 stack, float x, float y, boolean rect, boolean drawItemInSlot, float scale, float alpha, int z) {
        if (rect) {
            class_4587 matrix = MatrixStackCompat.from(context.method_51448());
            blur.render(ShapeProperties.create(matrix, x, y, 16.0f * scale + 2.0f, 16.0f * scale + 2.0f).round(2.0f).color(ColorUtil.HALF_BLACK).build());
        }
        Matrix3x2fStack matrices = context.method_51448();
        matrices.pushMatrix();
        matrices.translate(x + 1.0f, y + 1.0f);
        matrices.scale(scale, scale);
        float inheritedAlpha = LegacyRenderSystem.getShaderColor()[3] * Math.max(0.0f, Math.min(1.0f, alpha));
        ItemGuiAlpha.render(renderAlpha * inheritedAlpha, () -> MathUtil.setAlpha(inheritedAlpha, () -> {
            context.method_51428(stack, 0, 0, z);
            if (drawItemInSlot) {
                context.method_51431(Render2DUtil.mc.field_1772, stack, 0, 0);
            }
        }));
        matrices.popMatrix();
    }

    public static void drawStack(class_4587 matrix, class_1799 stack, float x, float y, boolean rect, float scale) {
        float posX = x + 1.0f;
        float posY = y + 1.0f;
        float padding = 1.0f;
        matrix.method_22903();
        matrix.method_46416(posX, posY, 0.0f);
        if (rect) {
            blur.render(ShapeProperties.create(matrix, -padding, -padding, 16.0f * scale + padding * 2.0f, 16.0f * scale + padding * 2.0f).round(1.5f).color(ColorUtil.HALF_BLACK).build());
        }
        matrix.method_22905(scale, scale, 1.0f);
        ItemRender.drawItem(matrix, stack, 0.0f, 0.0f, true, true);
        matrix.method_22909();
    }

    public static void drawTexture(class_332 context, class_2960 id, float x, float y, int size) {
        if (id != null && renderAlpha > 0.0f) {
            context.method_25293(class_10799.field_56883, id, Math.round(x), Math.round(y), 0.0f, 0.0f, size, size, size, size, size, size, Render2DUtil.applyRenderAlpha(-1));
        }
    }

    public static void drawTexture(class_332 context, class_2960 id, float x, float y, float size, float round, int uvSize, int regionSize, int textureSize, int backgroundColor) {
        Render2DUtil.drawTexture(context, id, x, y, size, round, uvSize, regionSize, textureSize, backgroundColor, -1);
    }

    public static void drawTexture(class_332 context, class_2960 id, float x, float y, float size, float round, int uvSize, int regionSize, int textureSize, int backgroundColor, int color) {
        int backgroundAlpha = Render2DUtil.applyRenderAlpha(backgroundColor);
        if (ColorUtil.alpha(backgroundAlpha) > 0) {
            class_4587 matrix = MatrixStackCompat.from(context.method_51448());
            rectangle.render(ShapeProperties.create(matrix, x, y, size, size).round(round).color(backgroundColor).build());
        }
        int textureAlpha = Render2DUtil.applyRenderAlpha(color);
        if (id != null && ColorUtil.alpha(textureAlpha) > 0) {
            int drawSize = Math.max(1, Math.round(size));
            context.method_25293(class_10799.field_56883, id, Math.round(x), Math.round(y), (float)uvSize, (float)uvSize, drawSize, drawSize, regionSize, regionSize, textureSize, textureSize, textureAlpha);
        }
    }

    public static void drawRoundedTexture(class_332 context, class_2960 id, float x, float y, float size, float round, int color) {
        Render2DUtil.drawRoundedTexture(context, id, x, y, size, round, 0.0f, 0.0f, 1, 1, 1, 1, color);
    }

    public static void drawRoundedTexture(class_332 context, class_2960 id, float x, float y, float size, float round, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color) {
        int alphaColor = Render2DUtil.applyRenderAlpha(color);
        if (id != null && ColorUtil.alpha(alphaColor) > 0) {
            class_4587 matrix = MatrixStackCompat.from(context.method_51448());
            ShapeShaderRenderer.renderRoundTexture(ShapeProperties.create(matrix, x, y, size, size).round(round).color(alphaColor).build(), id, alphaColor, u / (float)textureWidth, v / (float)textureHeight, (u + (float)regionWidth) / (float)textureWidth, (v + (float)regionHeight) / (float)textureHeight);
        }
    }

    public static void drawSprite(class_4587 matrix, class_1058 sprite, float x, float y, float width, int height) {
        if (width != 0.0f && height != 0) {
            Render2DUtil.drawTexturedQuad(matrix, sprite.method_45852(), x, x + width, y, y + (float)height, sprite.method_4594(), sprite.method_4577(), sprite.method_4593(), sprite.method_4575(), -1);
        }
    }

    public static void drawTexture(class_4587 matrix, class_2960 texture, int x, int y, float width, float height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color) {
        Render2DUtil.drawTexture(matrix, texture, x, (float)x + width, y, (float)y + height, 0.0f, regionWidth, regionHeight, u, v, textureWidth, textureHeight, color);
    }

    public static void drawTexture(class_4587 matrix, class_2960 texture, float x1, float x2, float y1, float y2, float z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight, int color) {
        Render2DUtil.drawTexturedQuad(matrix, texture, x1, x2, y1, y2, (u + 0.0f) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0f) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight, color);
    }

    public static void drawTexturedQuad(class_4587 matrix, class_2960 texture, float x1, float x2, float y1, float y2, float u1, float u2, float v1, float v2, int color) {
        if (ColorUtil.alpha(Render2DUtil.applyRenderAlpha(color)) <= 0) {
            return;
        }
        LegacyRenderSystem.setShaderTexture(0, texture);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        Matrix4f matrix4f = matrix.method_23760().method_23761();
        int alphaColor = Render2DUtil.applyRenderAlpha(color);
        buffer.method_22918((Matrix4fc)matrix4f, x1, y1, 0.0f).method_22913(u1, v1).method_39415(alphaColor);
        buffer.method_22918((Matrix4fc)matrix4f, x1, y2, 0.0f).method_22913(u1, v2).method_39415(alphaColor);
        buffer.method_22918((Matrix4fc)matrix4f, x2, y2, 0.0f).method_22913(u2, v2).method_39415(alphaColor);
        buffer.method_22918((Matrix4fc)matrix4f, x2, y1, 0.0f).method_22913(u2, v1).method_39415(alphaColor);
        BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
    }

    public static void drawQuad(float x, float y, float width, float height, int color) {
        QUAD.add(new Quad(x, y, width, height, ColorUtil.multAlpha(color, LegacyRenderSystem.getShaderColor()[3])));
    }

    private Render2DUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        renderAlpha = 1.0f;
    }

    public record Quad(float x, float y, float width, float height, int color) {
    }
}

