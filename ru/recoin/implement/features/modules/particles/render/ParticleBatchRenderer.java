/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_327$class_6415
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.recoin.implement.features.modules.particles.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_327;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.recoin.api.feature.module.IParticleModule;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.particle.ParticleColorUtil;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;

public class ParticleBatchRenderer
implements QuickImports {
    private static final class_2960 FIREFLY_TEXTURE = class_2960.method_60654((String)"textures/particles/firefly.png");
    private static final int MATRIX_POOL_SIZE = 8192;
    private static final int MATRIX_POOL_WARM_SIZE = 512;
    private static final Queue<Matrix4f> MATRIX_POOL = new ArrayDeque<Matrix4f>(8192);
    private static final Queue<TexturedParticleData> TEXTURED_PARTICLES;
    private static final Queue<CubeParticleData> CUBE_PARTICLES;
    private static final Queue<PyramidParticleData> PYRAMID_PARTICLES;
    private static final Queue<TextParticleData> TEXT_PARTICLES;
    private static final Queue<BloomParticleData> BLOOM_PARTICLES;
    private static final Queue<FireflyTrailData> FIREFLY_TRAILS;
    private static final Queue<BubbleParticleData> BUBBLE_PARTICLES;
    private static final Queue<Line3DParticleData> LINE_3D_PARTICLES;
    private static final Map<class_2960, List<TexturedParticleData>> TEXTURED_GROUPS;
    private static final Map<class_2960, List<AmbientTexturedParticleData>> AMBIENT_GROUPS;
    private static final Map<class_2960, List<BubbleParticleData>> BUBBLE_GROUPS;
    private static final float SQRT_3;
    private static final float SQRT_6;
    private static final Queue<AmbientTexturedParticleData> AMBIENT_TEXTURED_PARTICLES;

    private static Matrix4f acquireMatrix(Matrix4f source) {
        Matrix4f matrix = MATRIX_POOL.poll();
        if (matrix == null) {
            matrix = new Matrix4f();
        }
        return matrix.set((Matrix4fc)source);
    }

    private static void releaseMatrix(Matrix4f matrix) {
        if (MATRIX_POOL.size() < 8192) {
            MATRIX_POOL.offer(matrix);
        }
    }

    private static void clearGroupLists(Map<?, List<?>> groups2) {
        groups2.values().forEach(List::clear);
        groups2.clear();
    }

    public static void queueTexturedParticle(class_4587 matrices, class_2960 texture, float size, float alpha, int color, boolean useVertexGradient, int[] gradientColors, IParticleModule module) {
        TEXTURED_PARTICLES.add(new TexturedParticleData(ParticleBatchRenderer.acquireMatrix(matrices.method_23760().method_23761()), texture, size, alpha, color, useVertexGradient, gradientColors, module));
    }

    public static void queueCubeParticle(class_4587 matrices, float size, float alpha, int color) {
        CUBE_PARTICLES.add(new CubeParticleData(ParticleBatchRenderer.acquireMatrix(matrices.method_23760().method_23761()), size, alpha, color));
    }

    public static void queuePyramidParticle(class_4587 matrices, float size, float alpha, int color) {
        PYRAMID_PARTICLES.add(new PyramidParticleData(ParticleBatchRenderer.acquireMatrix(matrices.method_23760().method_23761()), size, alpha, color));
    }

    public static void queueTextParticle(class_4587 matrices, String text, int color) {
        TEXT_PARTICLES.add(new TextParticleData(ParticleBatchRenderer.acquireMatrix(matrices.method_23760().method_23761()), text, color));
    }

    public static void queueBloomParticle(class_4587 matrices, float size, float alpha, boolean useVertexGradient, int[] gradientColors, IParticleModule module, int colorOffset) {
        BLOOM_PARTICLES.add(new BloomParticleData(ParticleBatchRenderer.acquireMatrix(matrices.method_23760().method_23761()), size, alpha, useVertexGradient, gradientColors, module, colorOffset));
    }

    public static void queueFireflyTrail(Matrix4f matrix, float size, int color) {
        FIREFLY_TRAILS.add(new FireflyTrailData(ParticleBatchRenderer.acquireMatrix(matrix), size, color));
    }

    public static void queueBubble(class_4587 matrices, class_2960 texture, float scale, int color1, int color2, int color3, int color4) {
        BUBBLE_PARTICLES.add(new BubbleParticleData(ParticleBatchRenderer.acquireMatrix(matrices.method_23760().method_23761()), texture, scale, color1, color2, color3, color4));
    }

    public static void queueLine3DParticle(class_4587 matrices, float size, float alpha, int color) {
        LINE_3D_PARTICLES.add(new Line3DParticleData(ParticleBatchRenderer.acquireMatrix(matrices.method_23760().method_23761()), size, alpha, color));
    }

    public static void renderBatches() {
        if (!BLOOM_PARTICLES.isEmpty()) {
            ParticleBatchRenderer.renderBloomBatch();
        }
        if (!TEXTURED_PARTICLES.isEmpty()) {
            ParticleBatchRenderer.renderTexturedBatch();
        }
        if (!AMBIENT_TEXTURED_PARTICLES.isEmpty()) {
            ParticleBatchRenderer.renderAmbientTexturedBatch();
        }
        if (!FIREFLY_TRAILS.isEmpty()) {
            ParticleBatchRenderer.renderFireflyBatch();
        }
        if (!CUBE_PARTICLES.isEmpty()) {
            ParticleBatchRenderer.renderCubeBatch();
        }
        if (!PYRAMID_PARTICLES.isEmpty()) {
            ParticleBatchRenderer.renderPyramidBatch();
        }
        if (!TEXT_PARTICLES.isEmpty()) {
            ParticleBatchRenderer.renderTextBatch();
        }
        if (!BUBBLE_PARTICLES.isEmpty()) {
            ParticleBatchRenderer.renderBubbleBatch();
        }
        if (!LINE_3D_PARTICLES.isEmpty()) {
            ParticleBatchRenderer.renderLine3DBatch();
        }
    }

    private static void renderTexturedBatch() {
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        TEXTURED_GROUPS.clear();
        while (!TEXTURED_PARTICLES.isEmpty()) {
            TexturedParticleData data = TEXTURED_PARTICLES.poll();
            TEXTURED_GROUPS.computeIfAbsent(data.texture, k -> new ArrayList()).add(data);
        }
        TEXTURED_GROUPS.forEach((texture, particles) -> {
            LegacyRenderSystem.setShaderTexture(0, texture);
            class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
            for (TexturedParticleData data : particles) {
                if (data.useVertexGradient && data.gradientColors != null) {
                    int col1 = ColorUtil.multDark(ParticleColorUtil.getVertexGradientColor(0, data.gradientColors, data.alpha), data.alpha);
                    int col2 = ColorUtil.multDark(ParticleColorUtil.getVertexGradientColor(90, data.gradientColors, data.alpha), data.alpha);
                    int col3 = ColorUtil.multDark(ParticleColorUtil.getVertexGradientColor(180, data.gradientColors, data.alpha), data.alpha);
                    int col4 = ColorUtil.multDark(ParticleColorUtil.getVertexGradientColor(270, data.gradientColors, data.alpha), data.alpha);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, data.size, 0.0f).method_22913(0.0f, 1.0f).method_39415(col1);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, data.size, data.size, 0.0f).method_22913(1.0f, 1.0f).method_39415(col2);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, data.size, 0.0f, 0.0f).method_22913(1.0f, 0.0f).method_39415(col3);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, 0.0f, 0.0f).method_22913(0.0f, 0.0f).method_39415(col4);
                } else {
                    int col = ColorUtil.multDark(data.color, data.alpha);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, data.size, 0.0f).method_22913(0.0f, 1.0f).method_39415(col);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, data.size, data.size, 0.0f).method_22913(1.0f, 1.0f).method_39415(col);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, data.size, 0.0f, 0.0f).method_22913(1.0f, 0.0f).method_39415(col);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, 0.0f, 0.0f).method_22913(0.0f, 0.0f).method_39415(col);
                }
                ParticleBatchRenderer.releaseMatrix(data.matrix);
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        });
        LegacyRenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    private static void renderCubeBatch() {
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.depthMask(false);
        class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_29344, class_290.field_1576);
        while (!CUBE_PARTICLES.isEmpty()) {
            CubeParticleData data = CUBE_PARTICLES.poll();
            float r = (float)(data.color >> 16 & 0xFF) / 255.0f;
            float g = (float)(data.color >> 8 & 0xFF) / 255.0f;
            float b = (float)(data.color & 0xFF) / 255.0f;
            float a = (float)(data.color >> 24 & 0xFF) / 255.0f;
            float half = data.size / 2.0f;
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, -half).method_22915(r, g, b, a);
            ParticleBatchRenderer.releaseMatrix(data.matrix);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    private static void renderPyramidBatch() {
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.depthMask(false);
        class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_29344, class_290.field_1576);
        while (!PYRAMID_PARTICLES.isEmpty()) {
            PyramidParticleData data = PYRAMID_PARTICLES.poll();
            float r = (float)(data.color >> 16 & 0xFF) / 255.0f;
            float g = (float)(data.color >> 8 & 0xFF) / 255.0f;
            float b = (float)(data.color & 0xFF) / 255.0f;
            float a = (float)(data.color >> 24 & 0xFF) / 255.0f;
            float v1x = 0.0f;
            float v1y = 0.0f;
            float v1z = data.size * SQRT_6 / 4.0f;
            float v2x = -data.size / 2.0f;
            float v2y = -data.size * SQRT_3 / 6.0f;
            float v2z = -data.size * SQRT_6 / 12.0f;
            float v3x = data.size / 2.0f;
            float v3y = -data.size * SQRT_3 / 6.0f;
            float v3z = -data.size * SQRT_6 / 12.0f;
            float v4x = 0.0f;
            float v4y = data.size * SQRT_3 / 3.0f;
            float v4z = -data.size * SQRT_6 / 12.0f;
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v2x, v2y, v2z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v3x, v3y, v3z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v3x, v3y, v3z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v4x, v4y, v4z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v4x, v4y, v4z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v2x, v2y, v2z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v1x, v1y, v1z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v2x, v2y, v2z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v1x, v1y, v1z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v3x, v3y, v3z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v1x, v1y, v1z).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, v4x, v4y, v4z).method_22915(r, g, b, a);
            ParticleBatchRenderer.releaseMatrix(data.matrix);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    private static void renderTextBatch() {
        while (!TEXT_PARTICLES.isEmpty()) {
            TextParticleData data = TEXT_PARTICLES.poll();
            float textX = (float)(-ParticleBatchRenderer.mc.field_1772.method_1727(data.text)) / 2.0f;
            ParticleBatchRenderer.mc.field_1772.method_27521(data.text, textX, 0.0f, data.color, false, data.matrix, (class_4597)mc.method_22940().method_23000(), class_327.class_6415.field_33993, 0, 0xF000F0);
            ParticleBatchRenderer.releaseMatrix(data.matrix);
        }
    }

    private static void renderBloomBatch() {
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.setShaderTexture(0, class_2960.method_60654((String)"textures/bloom.png"));
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        while (!BLOOM_PARTICLES.isEmpty()) {
            BloomParticleData data = BLOOM_PARTICLES.poll();
            float half = data.size / 2.0f;
            float alpha = Math.max(0.0f, Math.min(1.0f, data.alpha));
            if (data.useVertexGradient && data.gradientColors != null && data.gradientColors.length > 0) {
                int col1 = ColorUtil.multAlpha(ParticleColorUtil.getVertexGradientColor(data.colorOffset + 0, data.gradientColors, 1.0f), alpha);
                int col2 = ColorUtil.multAlpha(ParticleColorUtil.getVertexGradientColor(data.colorOffset + 90, data.gradientColors, 1.0f), alpha);
                int col3 = ColorUtil.multAlpha(ParticleColorUtil.getVertexGradientColor(data.colorOffset + 180, data.gradientColors, 1.0f), alpha);
                int col4 = ColorUtil.multAlpha(ParticleColorUtil.getVertexGradientColor(data.colorOffset + 270, data.gradientColors, 1.0f), alpha);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, 0.0f).method_22913(0.0f, 1.0f).method_39415(col1);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, 0.0f).method_22913(1.0f, 1.0f).method_39415(col2);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, 0.0f).method_22913(1.0f, 0.0f).method_39415(col3);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, 0.0f).method_22913(0.0f, 0.0f).method_39415(col4);
            } else {
                int[] colors;
                int baseColor = data.module != null && data.module.getColorMode().isSelected("Sync") ? ColorUtil.getClientColor() : (data.module != null ? ((colors = data.module.getCustomColors()) != null && colors.length > 0 ? ParticleColorUtil.getWaveColor(colors, 1.0f, data.colorOffset) : -1) : -1);
                int color = ColorUtil.multAlpha(baseColor, alpha);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, 0.0f).method_22913(0.0f, 1.0f).method_39415(color);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, 0.0f).method_22913(1.0f, 1.0f).method_39415(color);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, 0.0f).method_22913(1.0f, 0.0f).method_39415(color);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, 0.0f).method_22913(0.0f, 0.0f).method_39415(color);
            }
            ParticleBatchRenderer.releaseMatrix(data.matrix);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    private static void renderAmbientTexturedBatch() {
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        AMBIENT_GROUPS.clear();
        while (!AMBIENT_TEXTURED_PARTICLES.isEmpty()) {
            AmbientTexturedParticleData data = AMBIENT_TEXTURED_PARTICLES.poll();
            AMBIENT_GROUPS.computeIfAbsent(data.texture, k -> new ArrayList()).add(data);
        }
        AMBIENT_GROUPS.forEach((texture, particles) -> {
            LegacyRenderSystem.setShaderTexture(0, texture);
            class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
            for (AmbientTexturedParticleData data : particles) {
                if (data.useVertexGradient && data.gradientColors != null) {
                    int col1 = ColorUtil.multDark(ParticleColorUtil.getVertexGradientColor(0, data.gradientColors, data.alpha), data.alpha);
                    int col2 = ColorUtil.multDark(ParticleColorUtil.getVertexGradientColor(90, data.gradientColors, data.alpha), data.alpha);
                    int col3 = ColorUtil.multDark(ParticleColorUtil.getVertexGradientColor(180, data.gradientColors, data.alpha), data.alpha);
                    int col4 = ColorUtil.multDark(ParticleColorUtil.getVertexGradientColor(270, data.gradientColors, data.alpha), data.alpha);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, data.size, 0.0f).method_22913(0.0f, 1.0f).method_39415(col1);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, data.size, data.size, 0.0f).method_22913(1.0f, 1.0f).method_39415(col2);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, data.size, 0.0f, 0.0f).method_22913(1.0f, 0.0f).method_39415(col3);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, 0.0f, 0.0f).method_22913(0.0f, 0.0f).method_39415(col4);
                } else {
                    int col = ColorUtil.multDark(data.color, data.alpha);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, data.size, 0.0f).method_22913(0.0f, 1.0f).method_39415(col);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, data.size, data.size, 0.0f).method_22913(1.0f, 1.0f).method_39415(col);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, data.size, 0.0f, 0.0f).method_22913(1.0f, 0.0f).method_39415(col);
                    bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, 0.0f, 0.0f).method_22913(0.0f, 0.0f).method_39415(col);
                }
                ParticleBatchRenderer.releaseMatrix(data.matrix);
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        });
        LegacyRenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    private static void renderFireflyBatch() {
        LegacyRenderSystem.setShaderTexture(0, FIREFLY_TEXTURE);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        while (!FIREFLY_TRAILS.isEmpty()) {
            FireflyTrailData data = FIREFLY_TRAILS.poll();
            bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, -data.size, 0.0f).method_22913(0.0f, 1.0f).method_39415(data.color);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -data.size, -data.size, 0.0f).method_22913(1.0f, 1.0f).method_39415(data.color);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -data.size, 0.0f, 0.0f).method_22913(1.0f, 0.0f).method_39415(data.color);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, 0.0f, 0.0f).method_22913(0.0f, 0.0f).method_39415(data.color);
            ParticleBatchRenderer.releaseMatrix(data.matrix);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    public static void queueAmbientTexturedParticle(class_4587 matrices, class_2960 texture, float size, float alpha, int color, boolean useVertexGradient, int[] gradientColors, IParticleModule module) {
        AMBIENT_TEXTURED_PARTICLES.add(new AmbientTexturedParticleData(ParticleBatchRenderer.acquireMatrix(matrices.method_23760().method_23761()), texture, size, alpha, color, useVertexGradient, gradientColors, module));
    }

    private static void renderLine3DBatch() {
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.depthMask(false);
        class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_29344, class_290.field_1576);
        while (!LINE_3D_PARTICLES.isEmpty()) {
            Line3DParticleData data = LINE_3D_PARTICLES.poll();
            float r = (float)(data.color >> 16 & 0xFF) / 255.0f;
            float g = (float)(data.color >> 8 & 0xFF) / 255.0f;
            float b = (float)(data.color & 0xFF) / 255.0f;
            float a = (float)(data.color >> 24 & 0xFF) / 255.0f;
            float half = data.size / 2.0f;
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, 0.0f, 0.0f).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, half, 0.0f, 0.0f).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, -half, 0.0f).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, half, 0.0f).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, 0.0f, -half).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, 0.0f, 0.0f, half).method_22915(r, g, b, a);
            float d = half * 0.7071f;
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -d, -d, -d).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, d, d, d).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, d, -d, -d).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -d, d, d).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -d, d, -d).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, d, -d, d).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, d, d, -d).method_22915(r, g, b, a);
            bufferBuilder.method_22918((Matrix4fc)data.matrix, -d, -d, d).method_22915(r, g, b, a);
            ParticleBatchRenderer.releaseMatrix(data.matrix);
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    private static void renderBubbleBatch() {
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthFunc(515);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        BUBBLE_GROUPS.clear();
        while (!BUBBLE_PARTICLES.isEmpty()) {
            BubbleParticleData data = BUBBLE_PARTICLES.poll();
            BUBBLE_GROUPS.computeIfAbsent(data.texture, k -> new ArrayList()).add(data);
        }
        BUBBLE_GROUPS.forEach((texture, bubbles) -> {
            LegacyRenderSystem.setShaderTexture(0, texture);
            class_287 bufferBuilder = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
            for (BubbleParticleData data : bubbles) {
                float half = data.scale / 2.0f;
                bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, half, 0.0f).method_22913(0.0f, 1.0f).method_39415(data.color1);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, half, half, 0.0f).method_22913(1.0f, 1.0f).method_39415(data.color2);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, half, -half, 0.0f).method_22913(1.0f, 0.0f).method_39415(data.color3);
                bufferBuilder.method_22918((Matrix4fc)data.matrix, -half, -half, 0.0f).method_22913(0.0f, 0.0f).method_39415(data.color4);
                ParticleBatchRenderer.releaseMatrix(data.matrix);
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.method_60800());
        });
        LegacyRenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.depthFunc(513);
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.disableDepthTest();
    }

    static {
        for (int i = 0; i < 512; ++i) {
            MATRIX_POOL.offer(new Matrix4f());
        }
        TEXTURED_PARTICLES = new ArrayDeque<TexturedParticleData>();
        CUBE_PARTICLES = new ArrayDeque<CubeParticleData>();
        PYRAMID_PARTICLES = new ArrayDeque<PyramidParticleData>();
        TEXT_PARTICLES = new ArrayDeque<TextParticleData>();
        BLOOM_PARTICLES = new ArrayDeque<BloomParticleData>();
        FIREFLY_TRAILS = new ArrayDeque<FireflyTrailData>();
        BUBBLE_PARTICLES = new ArrayDeque<BubbleParticleData>();
        LINE_3D_PARTICLES = new ArrayDeque<Line3DParticleData>();
        TEXTURED_GROUPS = new HashMap<class_2960, List<TexturedParticleData>>();
        AMBIENT_GROUPS = new HashMap<class_2960, List<AmbientTexturedParticleData>>();
        BUBBLE_GROUPS = new HashMap<class_2960, List<BubbleParticleData>>();
        SQRT_3 = (float)Math.sqrt(3.0);
        SQRT_6 = (float)Math.sqrt(6.0);
        AMBIENT_TEXTURED_PARTICLES = new ArrayDeque<AmbientTexturedParticleData>();
    }

    private record TexturedParticleData(Matrix4f matrix, class_2960 texture, float size, float alpha, int color, boolean useVertexGradient, int[] gradientColors, IParticleModule module) {
    }

    private record CubeParticleData(Matrix4f matrix, float size, float alpha, int color) {
    }

    private record PyramidParticleData(Matrix4f matrix, float size, float alpha, int color) {
    }

    private record TextParticleData(Matrix4f matrix, String text, int color) {
    }

    private record BloomParticleData(Matrix4f matrix, float size, float alpha, boolean useVertexGradient, int[] gradientColors, IParticleModule module, int colorOffset) {
    }

    private record FireflyTrailData(Matrix4f matrix, float size, int color) {
    }

    private record BubbleParticleData(Matrix4f matrix, class_2960 texture, float scale, int color1, int color2, int color3, int color4) {
    }

    private record Line3DParticleData(Matrix4f matrix, float size, float alpha, int color) {
    }

    private record AmbientTexturedParticleData(Matrix4f matrix, class_2960 texture, float size, float alpha, int color, boolean useVertexGradient, int[] gradientColors, IParticleModule module) {
    }
}

