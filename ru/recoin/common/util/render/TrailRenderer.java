/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.Render3DUtil;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.features.modules.render.Trails;

public final class TrailRenderer
implements QuickImports {
    private static final ConcurrentLinkedQueue<TrailData> TRAIL_QUEUE = new ConcurrentLinkedQueue();
    private static final ConcurrentLinkedQueue<DebugLineData> DEBUG_LINE_QUEUE = new ConcurrentLinkedQueue();
    private static final ConcurrentLinkedQueue<TailData> TAIL_QUEUE = new ConcurrentLinkedQueue();
    private static final class_2960 FIREFLY = class_2960.method_60654((String)"textures/particles/firefly.png");

    public static void queueTrails(class_4587 stack, List<Trails.Trail> trails, Trails module) {
        if (trails.size() < 2) {
            return;
        }
        TRAIL_QUEUE.add(new TrailData(new Matrix4f((Matrix4fc)stack.method_23760().method_23761()), trails, module));
    }

    public static void queueDebugLines(class_4587 stack, List<Trails.Trail> trails, Trails module) {
        if (trails.size() < 2) {
            return;
        }
        DEBUG_LINE_QUEUE.add(new DebugLineData(new Matrix4f((Matrix4fc)stack.method_23760().method_23761()), trails, module));
    }

    public static void queueTails(class_4587 stack, List<Trails.Trail> trails, Trails module) {
        if (trails.isEmpty()) {
            return;
        }
        TAIL_QUEUE.add(new TailData(new Matrix4f((Matrix4fc)stack.method_23760().method_23761()), trails, module));
    }

    public static void renderBatches() {
        if (!TRAIL_QUEUE.isEmpty()) {
            TrailRenderer.renderTrailBatch();
        }
        if (!DEBUG_LINE_QUEUE.isEmpty()) {
            TrailRenderer.renderDebugLineBatch();
        }
        if (!TAIL_QUEUE.isEmpty()) {
            TrailRenderer.renderTailBatch();
        }
    }

    private static void renderTrailBatch() {
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.blendFunc(770, 771);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        class_243 cameraPos = TrailRenderer.mc.field_1773.method_19418().method_71156();
        while (!TRAIL_QUEUE.isEmpty()) {
            TrailData data = TRAIL_QUEUE.poll();
            List<Trails.Trail> trails = data.trails;
            Trails module = data.module;
            Matrix4f matrix = data.matrix;
            float heightPercent = 1.0f;
            int baseAlpha = (int)(255.0f * (module.trailAlpha.getValue() / 100.0f));
            for (int i = 0; i < trails.size() - 1; ++i) {
                int nextTopAlpha;
                int nextMidAlpha;
                int nextBottomAlpha;
                int currentTopAlpha;
                int currentMidAlpha;
                int currentBottomAlpha;
                Trails.Trail current = trails.get(i);
                Trails.Trail next = trails.get(i + 1);
                class_243 currentPos = current.currentPos;
                class_243 nextPos = next.currentPos;
                float currentAlpha = current.getAlpha();
                float nextAlpha = next.getAlpha();
                float currentAlphaFactor = currentAlpha * (float)baseAlpha;
                float nextAlphaFactor = nextAlpha * (float)baseAlpha;
                float currentProgress = current.getProgress();
                float nextProgress = next.getProgress();
                Color currentColor = TrailRenderer.getAnimatedColor(current, module, currentProgress, 1.0f - currentProgress);
                Color nextColor = TrailRenderer.getAnimatedColor(next, module, nextProgress, 1.0f - nextProgress);
                float currentTrailHeight = current.entityHeight * heightPercent;
                float nextTrailHeight = next.entityHeight * heightPercent;
                float currentCenterOffset = current.entityHeight / 2.0f;
                float nextCenterOffset = next.entityHeight / 2.0f;
                float x1 = (float)(currentPos.field_1352 - cameraPos.field_1352);
                float y1 = (float)(currentPos.field_1351 - cameraPos.field_1351) + currentCenterOffset - currentTrailHeight / 2.0f + 0.0f;
                float z1 = (float)(currentPos.field_1350 - cameraPos.field_1350);
                float x2 = (float)(nextPos.field_1352 - cameraPos.field_1352);
                float y2 = (float)(nextPos.field_1351 - cameraPos.field_1351) + nextCenterOffset - nextTrailHeight / 2.0f + 0.0f;
                float z2 = (float)(nextPos.field_1350 - cameraPos.field_1350);
                if (module.style.isSelected("Faded")) {
                    currentBottomAlpha = (int)((float)TrailRenderer.computeFadedAlpha(0.0f, currentTrailHeight) * (currentAlphaFactor / 255.0f));
                    currentMidAlpha = (int)((float)TrailRenderer.computeFadedAlpha(currentTrailHeight / 2.0f, currentTrailHeight) * (currentAlphaFactor / 255.0f));
                    currentTopAlpha = (int)((float)TrailRenderer.computeFadedAlpha(currentTrailHeight, currentTrailHeight) * (currentAlphaFactor / 255.0f));
                    nextBottomAlpha = (int)((float)TrailRenderer.computeFadedAlpha(0.0f, nextTrailHeight) * (nextAlphaFactor / 255.0f));
                    nextMidAlpha = (int)((float)TrailRenderer.computeFadedAlpha(nextTrailHeight / 2.0f, nextTrailHeight) * (nextAlphaFactor / 255.0f));
                    nextTopAlpha = (int)((float)TrailRenderer.computeFadedAlpha(nextTrailHeight, nextTrailHeight) * (nextAlphaFactor / 255.0f));
                } else if (module.style.isSelected("Invert")) {
                    float alphaFactorValue = module.alphaFactor.getValue();
                    currentBottomAlpha = (int)((float)TrailRenderer.computeFadedAlphaInvert(0.0f, currentTrailHeight, alphaFactorValue) * (currentAlphaFactor / 255.0f));
                    currentMidAlpha = (int)((float)TrailRenderer.computeFadedAlphaInvert(currentTrailHeight / 2.0f, currentTrailHeight, alphaFactorValue) * (currentAlphaFactor / 255.0f));
                    currentTopAlpha = (int)((float)TrailRenderer.computeFadedAlphaInvert(currentTrailHeight, currentTrailHeight, alphaFactorValue) * (currentAlphaFactor / 255.0f));
                    nextBottomAlpha = (int)((float)TrailRenderer.computeFadedAlphaInvert(0.0f, nextTrailHeight, alphaFactorValue) * (nextAlphaFactor / 255.0f));
                    nextMidAlpha = (int)((float)TrailRenderer.computeFadedAlphaInvert(nextTrailHeight / 2.0f, nextTrailHeight, alphaFactorValue) * (nextAlphaFactor / 255.0f));
                    nextTopAlpha = (int)((float)TrailRenderer.computeFadedAlphaInvert(nextTrailHeight, nextTrailHeight, alphaFactorValue) * (nextAlphaFactor / 255.0f));
                } else {
                    currentBottomAlpha = (int)currentAlphaFactor;
                    currentMidAlpha = (int)currentAlphaFactor;
                    currentTopAlpha = (int)currentAlphaFactor;
                    nextBottomAlpha = (int)nextAlphaFactor;
                    nextMidAlpha = (int)nextAlphaFactor;
                    nextTopAlpha = (int)nextAlphaFactor;
                }
                bufferBuilder.method_22918((Matrix4fc)matrix, x1, y1, z1).method_1336(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), currentBottomAlpha);
                bufferBuilder.method_22918((Matrix4fc)matrix, x2, y2, z2).method_1336(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nextBottomAlpha);
                bufferBuilder.method_22918((Matrix4fc)matrix, x2, y2 + nextTrailHeight / 2.0f, z2).method_1336(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nextMidAlpha);
                bufferBuilder.method_22918((Matrix4fc)matrix, x1, y1 + currentTrailHeight / 2.0f, z1).method_1336(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), currentMidAlpha);
                bufferBuilder.method_22918((Matrix4fc)matrix, x1, y1 + currentTrailHeight / 2.0f, z1).method_1336(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), currentMidAlpha);
                bufferBuilder.method_22918((Matrix4fc)matrix, x2, y2 + nextTrailHeight / 2.0f, z2).method_1336(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nextMidAlpha);
                bufferBuilder.method_22918((Matrix4fc)matrix, x2, y2 + nextTrailHeight, z2).method_1336(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nextTopAlpha);
                bufferBuilder.method_22918((Matrix4fc)matrix, x1, y1 + currentTrailHeight, z1).method_1336(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), currentTopAlpha);
            }
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
    }

    private static void renderDebugLineBatch() {
        while (!DEBUG_LINE_QUEUE.isEmpty()) {
            DebugLineData data = DEBUG_LINE_QUEUE.poll();
            List<Trails.Trail> trails = data.trails;
            Trails module = data.module;
            float heightPercent = 1.0f;
            for (int i = 0; i < trails.size() - 1; ++i) {
                Trails.Trail current = trails.get(i);
                Trails.Trail next = trails.get(i + 1);
                class_243 currentPos = current.currentPos;
                class_243 nextPos = next.currentPos;
                float currentTrailHeight = current.entityHeight * heightPercent;
                float nextTrailHeight = next.entityHeight * heightPercent;
                float currentCenterOffset = current.entityHeight / 2.0f;
                float nextCenterOffset = next.entityHeight / 2.0f;
                float currentBottomY = currentCenterOffset - currentTrailHeight / 2.0f + 0.0f;
                float currentTopY = currentCenterOffset + currentTrailHeight / 2.0f + 0.0f;
                float nextBottomY = nextCenterOffset - nextTrailHeight / 2.0f + 0.0f;
                float nextTopY = nextCenterOffset + nextTrailHeight / 2.0f + 0.0f;
                float alpha = 2.5554f;
                int currentColor = ColorUtil.multAlpha(TrailRenderer.getAnimatedColor(current, module, current.getProgress(), 1.0f - current.getProgress()).getRGB(), current.getAlpha() * alpha);
                int nextColor = ColorUtil.multAlpha(TrailRenderer.getAnimatedColor(next, module, next.getProgress(), 1.0f - next.getProgress()).getRGB(), next.getAlpha() * alpha);
                float width = 1.27f;
                class_243 bottomStart = new class_243(currentPos.field_1352, currentPos.field_1351 + (double)currentBottomY, currentPos.field_1350);
                class_243 bottomEnd = new class_243(nextPos.field_1352, nextPos.field_1351 + (double)nextBottomY, nextPos.field_1350);
                Render3DUtil.drawLine(null, bottomStart, bottomEnd, currentColor, nextColor, width, true);
                class_243 topStart = new class_243(currentPos.field_1352, currentPos.field_1351 + (double)currentTopY, currentPos.field_1350);
                class_243 topEnd = new class_243(nextPos.field_1352, nextPos.field_1351 + (double)nextTopY, nextPos.field_1350);
                Render3DUtil.drawLine(null, topStart, topEnd, currentColor, nextColor, width, true);
            }
        }
    }

    private static void renderTailBatch() {
        class_4184 camera = TrailRenderer.mc.field_1773.method_19418();
        LegacyRenderSystem.setShaderTexture(0, FIREFLY);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        while (!TAIL_QUEUE.isEmpty()) {
            TailData data = TAIL_QUEUE.poll();
            for (Trails.Trail trail : data.trails) {
                class_4587 matrices = new class_4587();
                class_243 pos = trail.currentPos;
                class_243 cameraPos = camera.method_71156();
                matrices.method_22904(pos.field_1352 - cameraPos.field_1352, pos.field_1351 - cameraPos.field_1351 + (double)0.9f, pos.field_1350 - cameraPos.field_1350);
                matrices.method_22907((Quaternionfc)camera.method_23767());
                Matrix4f matrix = matrices.method_23760().method_23761();
                float size = 0.6f;
                float animAlpha = trail.getAlpha();
                Color col = TrailRenderer.interpolateColor(trail.color(), Color.WHITE, (float)Math.pow(1.0f - trail.getProgress(), 2.0));
                int finalColor = ColorUtil.replAlpha(col.getRGB(), (int)(animAlpha * 255.0f));
                bufferBuilder.method_22918((Matrix4fc)matrix, -size, size, 0.0f).method_22913(0.0f, 1.0f).method_39415(finalColor);
                bufferBuilder.method_22918((Matrix4fc)matrix, size, size, 0.0f).method_22913(1.0f, 1.0f).method_39415(finalColor);
                bufferBuilder.method_22918((Matrix4fc)matrix, size, -size, 0.0f).method_22913(1.0f, 0.0f).method_39415(finalColor);
                bufferBuilder.method_22918((Matrix4fc)matrix, -size, -size, 0.0f).method_22913(0.0f, 0.0f).method_39415(finalColor);
            }
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
        LegacyRenderSystem.disableBlend();
    }

    private static int getWaveColor(int[] colors, float alpha) {
        if (colors.length == 1) {
            return ColorUtil.multAlpha(colors[0], alpha);
        }
        if (colors.length == 2) {
            int angle = (int)(System.currentTimeMillis() / 8L % 360L);
            angle = angle >= 180 ? 360 - angle : angle;
            return ColorUtil.multAlpha(ColorUtil.overCol(colors[0], colors[1], (float)angle / 180.0f), alpha);
        }
        float timeProgress = (float)(System.currentTimeMillis() / 10L % ((long)colors.length * 360L)) / 360.0f;
        int index1 = (int)Math.floor(timeProgress) % colors.length;
        int index2 = (index1 + 1) % colors.length;
        float lerp = timeProgress - (float)((int)Math.floor(timeProgress));
        return ColorUtil.multAlpha(ColorUtil.overCol(colors[index1], colors[index2], lerp), alpha);
    }

    private static int getVertexGradientColor(int angleOffset, int[] colors, float alpha) {
        if (colors.length == 1) {
            return ColorUtil.multAlpha(colors[0], alpha);
        }
        int angle = (int)((System.currentTimeMillis() / 8L + (long)angleOffset) % 360L);
        if (colors.length == 2) {
            angle = angle >= 180 ? 360 - angle : angle;
            return ColorUtil.multAlpha(ColorUtil.overCol(colors[0], colors[1], (float)angle / 180.0f), alpha);
        }
        float progress = (float)angle / 360.0f;
        float colorIndex = progress * (float)colors.length;
        int index1 = (int)colorIndex % colors.length;
        int index2 = (index1 + 1) % colors.length;
        float lerp = colorIndex - (float)((int)colorIndex);
        return ColorUtil.multAlpha(ColorUtil.overCol(colors[index1], colors[index2], lerp), alpha);
    }

    private static int computeFadedAlpha(float yOffset, float height) {
        float yRelative = yOffset / height;
        if (yRelative <= 0.5f) {
            return (int)((1.0f - yRelative / 0.5f) * 255.0f);
        }
        return (int)((yRelative - 0.5f) / 0.5f * 255.0f);
    }

    private static int computeFadedAlphaInvert(float yOffset, float height, float alphaFactorPercent) {
        float yRelative = yOffset / height;
        int alphaFactor = (int)(255.0f * (alphaFactorPercent / 100.0f));
        if (yRelative <= 0.5f) {
            return (int)((float)alphaFactor + yRelative / 0.5f * (float)(255 - alphaFactor));
        }
        return (int)(255.0f - (yRelative - 0.5f) / 0.5f * (float)(255 - alphaFactor));
    }

    private static Color getAnimatedColor(Trails.Trail trail, Trails module, float x, float y) {
        Color c1 = new Color(ColorUtil.fade(270), true);
        Color c2 = new Color(ColorUtil.fade(0), true);
        Color c3 = new Color(ColorUtil.fade(180), true);
        Color c4 = new Color(ColorUtil.fade(90), true);
        Color top = TrailRenderer.lerpColor(c1, c2, x);
        Color bottom = TrailRenderer.lerpColor(c4, c3, x);
        return TrailRenderer.lerpColor(top, bottom, y);
    }

    private static Color interpolateColor(Color c1, Color c2, float percent) {
        int r = (int)((float)c1.getRed() + (float)(c2.getRed() - c1.getRed()) * percent);
        int g = (int)((float)c1.getGreen() + (float)(c2.getGreen() - c1.getGreen()) * percent);
        int b = (int)((float)c1.getBlue() + (float)(c2.getBlue() - c1.getBlue()) * percent);
        return new Color(r, g, b);
    }

    private static Color lerpColor(Color a, Color b, float t) {
        int r = (int)((float)a.getRed() + (float)(b.getRed() - a.getRed()) * t);
        int g = (int)((float)a.getGreen() + (float)(b.getGreen() - a.getGreen()) * t);
        int blue = (int)((float)a.getBlue() + (float)(b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, blue);
    }

    private TrailRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private record TrailData(Matrix4f matrix, List<Trails.Trail> trails, Trails module) {
    }

    private record DebugLineData(Matrix4f matrix, List<Trails.Trail> trails, Trails module) {
    }

    private record TailData(Matrix4f matrix, List<Trails.Trail> trails, Trails module) {
    }
}

