/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1657
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_2382
 *  net.minecraft.class_243
 *  net.minecraft.class_265
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_3532
 *  net.minecraft.class_3545
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_7833
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4i
 */
package ru.recoin.common.util.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2382;
import net.minecraft.class_243;
import net.minecraft.class_265;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3545;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_7833;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4i;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.math.ProjectionUtil;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.shader.Shader;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.events.render.WorldRenderEvent;
import ru.recoin.implement.features.modules.particles.render.ParticleBatchRenderer;

public final class Render3DUtil
implements QuickImports {
    private static final Map<class_265, class_3545<List<class_238>, List<Line>>> SHAPE_OUTLINES = new HashMap<class_265, class_3545<List<class_238>, List<Line>>>();
    private static final Map<class_265, List<class_238>> SHAPE_BOXES = new HashMap<class_265, List<class_238>>();
    public static final List<Texture> TEXTURE_DEPTH = new ArrayList<Texture>();
    /** Textured panels with normal UVs (u right, v down), e.g. the in-world Browser window. Depth tested. */
    public static final List<Texture> PAGE = new ArrayList<Texture>();
    public static final List<Texture> TEXTURE = new ArrayList<Texture>();
    public static final List<Line> LINE_DEPTH = new ArrayList<Line>();
    public static final List<Line> LINE = new ArrayList<Line>();
    public static final List<Line> ADDITIVE_LINE_DEPTH = new ArrayList<Line>();
    public static final List<Line> ADDITIVE_LINE = new ArrayList<Line>();
    public static final List<LightningSegment> LIGHTNING_DEPTH = new ArrayList<LightningSegment>();
    public static final List<LightningSegment> LIGHTNING = new ArrayList<LightningSegment>();
    public static final List<Quad> QUAD_DEPTH = new ArrayList<Quad>();
    public static final List<Quad> QUAD = new ArrayList<Quad>();
    public static final List<GradientQuad> GRADIENT_QUAD_DEPTH = new ArrayList<GradientQuad>();
    public static final List<GradientQuad> GRADIENT_QUAD = new ArrayList<GradientQuad>();
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final class_4587.class_4665 lastWorldSpaceMatrix = new class_4587().method_23760();
    private static final class_2960 captureId = class_2960.method_60654((String)"textures/capture.png");
    private static final class_2960 targetN2 = class_2960.method_60654((String)"textures/targetn2.png");
    private static final class_2960 bloom = class_2960.method_60654((String)"textures/bloom.png");
    private static final class_2960 glow = class_2960.method_60654((String)"textures/glow.png");
    private static final double[] GHOST_RADII = new double[]{0.62, 0.58, 0.56};
    private static final double[] GHOST_PHASES = new double[]{0.0, 0.33, 0.66};
    private static final int GHOST_SEGMENTS = 47;
    private static final double[] GHOST_STRENGTH_SIZE = new double[47];
    private static final double[] GHOST_STRENGTH_WHITE = new double[47];
    private static final double[] GHOST_STRENGTH_COLOR = new double[47];
    private static final Vector3f[] CRYSTAL_VERTICES;
    private static final int[][] CRYSTAL_FACES;
    private static final float[] CRYSTAL_BRIGHTNESS;
    private static Shader lightningShader;
    private static boolean lightningShaderFailed;
    private static float espValue;
    private static float espSpeed;
    private static float prevEspValue;
    private static float circleStep;
    private static float prevCrystalEspValue;
    private static float crystalEspValue;
    private static float circleHitValue;
    private static int targetEspUpdateAge;
    private static boolean flipSpeed;
    private static double circleSmoothY;
    private static double circleSmoothY2;
    private static double crystalCircleY;
    private static double crystalCircleOffset;
    private static long crystalCircleFrameTime;
    private static double blackFlashPhase;
    private static long blackFlashLastTime;

    public static void setLastProjMat(Matrix4f mat) {
        if (mat != null) {
            lastProjMat.set((Matrix4fc)mat);
        }
    }

    public static void setLastWorldSpaceMatrix(class_4587.class_4665 entry) {
        if (entry != null) {
            lastWorldSpaceMatrix.method_23761().set((Matrix4fc)entry.method_23761());
            lastWorldSpaceMatrix.method_23762().set((Matrix3fc)entry.method_23762());
        }
    }

    public static void onWorldRender(WorldRenderEvent e) {
        class_287 buffer;
        ParticleBatchRenderer.renderBatches();
        if (!PAGE.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableCull();
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            for (Texture page : PAGE) {
                LegacyRenderSystem.setShaderTexture(0, page.id);
                LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                class_287 pb = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
                int c = page.color.x;
                pb.method_56824(page.entry, page.x, page.y + page.height, 0.0f).method_22913(0.0f, 0.0f).method_39415(c);
                pb.method_56824(page.entry, page.x + page.width, page.y + page.height, 0.0f).method_22913(1.0f, 0.0f).method_39415(c);
                pb.method_56824(page.entry, page.x + page.width, page.y, 0.0f).method_22913(1.0f, 1.0f).method_39415(c);
                pb.method_56824(page.entry, page.x, page.y, 0.0f).method_22913(0.0f, 1.0f).method_39415(c);
                BufferRenderer.drawWithGlobalProgram(pb.method_60800());
            }
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.disableBlend();
            PAGE.clear();
        }
        if (!TEXTURE.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableDepthTest();
            Render3DUtil.renderTextureBatches(TEXTURE);
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.disableBlend();
            TEXTURE.clear();
        }
        if (!TEXTURE_DEPTH.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.depthFunc(515);
            LegacyRenderSystem.depthMask(false);
            Render3DUtil.renderTextureBatches(TEXTURE_DEPTH);
            LegacyRenderSystem.depthMask(true);
            LegacyRenderSystem.depthFunc(513);
            LegacyRenderSystem.disableBlend();
            TEXTURE_DEPTH.clear();
        }
        if (!GRADIENT_QUAD.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableCull();
            LegacyRenderSystem.disableDepthTest();
            LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
            class_287 buffer0 = buffer;
            GRADIENT_QUAD.forEach(quad -> Render3DUtil.vertexGradientQuad(quad.entry, (class_4588)buffer0, quad.x, quad.y, quad.w, quad.z, quad.colorX, quad.colorY, quad.colorW, quad.colorZ));
            BufferRenderer.drawWithGlobalProgram(buffer0.method_60800());
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.disableBlend();
            GRADIENT_QUAD.clear();
        }
        if (!LINE.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableCull();
            LegacyRenderSystem.disableDepthTest();
            LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Render3DUtil.renderLineBatches(LINE);
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.disableBlend();
            LINE.clear();
        }
        if (!ADDITIVE_LINE.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableCull();
            LegacyRenderSystem.disableDepthTest();
            LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Render3DUtil.renderLineBatches(ADDITIVE_LINE);
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.disableBlend();
            ADDITIVE_LINE.clear();
        }
        Render3DUtil.renderLightningSegments(LIGHTNING, false);
        if (!QUAD.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableCull();
            LegacyRenderSystem.disableDepthTest();
            LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
            class_287 buffer1 = buffer;
            QUAD.forEach(quad -> Render3DUtil.vertexQuad(quad.entry, (class_4588)buffer1, quad.x, quad.y, quad.w, quad.z, quad.color));
            BufferRenderer.drawWithGlobalProgram(buffer1.method_60800());
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.disableBlend();
            QUAD.clear();
        }
        if (!LINE_DEPTH.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableCull();
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.depthFunc(515);
            LegacyRenderSystem.depthMask(false);
            LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Render3DUtil.renderLineBatches(LINE_DEPTH);
            LegacyRenderSystem.depthMask(true);
            LegacyRenderSystem.depthFunc(513);
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.disableBlend();
            LINE_DEPTH.clear();
        }
        if (!ADDITIVE_LINE_DEPTH.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableCull();
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.depthFunc(515);
            LegacyRenderSystem.depthMask(false);
            LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Render3DUtil.renderLineBatches(ADDITIVE_LINE_DEPTH);
            LegacyRenderSystem.depthMask(true);
            LegacyRenderSystem.depthFunc(513);
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.disableBlend();
            ADDITIVE_LINE_DEPTH.clear();
        }
        Render3DUtil.renderLightningSegments(LIGHTNING_DEPTH, true);
        if (!GRADIENT_QUAD_DEPTH.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableCull();
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.depthFunc(515);
            LegacyRenderSystem.depthMask(false);
            LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
            class_287 buffer2 = buffer;
            GRADIENT_QUAD_DEPTH.forEach(quad -> Render3DUtil.vertexGradientQuad(quad.entry, (class_4588)buffer2, quad.x, quad.y, quad.w, quad.z, quad.colorX, quad.colorY, quad.colorW, quad.colorZ));
            BufferRenderer.drawWithGlobalProgram(buffer2.method_60800());
            LegacyRenderSystem.depthMask(true);
            LegacyRenderSystem.depthFunc(513);
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.disableBlend();
            GRADIENT_QUAD_DEPTH.clear();
        }
        if (!QUAD_DEPTH.isEmpty()) {
            LegacyRenderSystem.enableBlend();
            LegacyRenderSystem.disableCull();
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
            class_287 buffer3 = buffer;
            QUAD_DEPTH.forEach(quad -> Render3DUtil.vertexQuad(quad.entry, (class_4588)buffer3, quad.x, quad.y, quad.w, quad.z, quad.color));
            BufferRenderer.drawWithGlobalProgram(buffer3.method_60800());
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.disableBlend();
            QUAD_DEPTH.clear();
        }
    }

    private static void renderTextureBatches(List<Texture> textures) {
        LinkedHashMap<TextureBatch, List> batches = new LinkedHashMap<TextureBatch, List>();
        for (Texture texture : textures) {
            batches.computeIfAbsent(new TextureBatch(texture.id, texture.additiveBlend), key -> new ArrayList()).add(texture);
        }
        for (Map.Entry entry : batches.entrySet()) {
            class_2960 id = ((TextureBatch)entry.getKey()).id();
            boolean additiveBlend = ((TextureBatch)entry.getKey()).additiveBlend();
            if (additiveBlend) {
                LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
            } else {
                LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            }
            LegacyRenderSystem.setShaderTexture(0, id);
            LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
            for (Texture texture : (List<Texture>)entry.getValue()) {
                Render3DUtil.quadTexture(texture.entry, buffer, texture.x, texture.y, texture.width, texture.height, texture.color);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
        }
    }

    private static void renderLineBatches(List<Line> lines) {
        float width;
        LinkedHashMap<Float, List> batches = new LinkedHashMap<Float, List>();
        for (Line line : lines) {
            width = line.width == 0.0f ? 0.0f : line.width;
            batches.computeIfAbsent(Float.valueOf(width), key -> new ArrayList()).add(line);
        }
        for (Map.Entry entry : batches.entrySet()) {
            width = ((Float)entry.getKey()).floatValue();
            LegacyRenderSystem.lineWidth(width);
            class_287 buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_29344, class_290.field_1576);
            for (Line line : (List<Line>)entry.getValue()) {
                Render3DUtil.vertexDebugLine(line.entry, (class_4588)buffer, line.start, line.end, line.colorStart, line.colorEnd);
            }
            BufferRenderer.drawWithGlobalProgram(buffer.method_60800());
        }
    }

    public static void drawShape(class_2338 blockPos, class_265 voxelShape, int color, float width) {
        Render3DUtil.drawShape(blockPos, voxelShape, color, width, true, false);
    }

    public static void drawShape(class_2338 blockPos, class_265 voxelShape, int color, float width, boolean fill, boolean depth) {
        if (SHAPE_BOXES.size() > 1024) {
            SHAPE_BOXES.clear();
        }
        List boxes = SHAPE_BOXES.computeIfAbsent(voxelShape, class_265::method_1090);
        int size = boxes.size();
        for (int i = 0; i < size; ++i) {
            class_238 worldBox = ((class_238)boxes.get(i)).method_996(blockPos);
            if (!ProjectionUtil.canSee(worldBox)) continue;
            Render3DUtil.drawBox(worldBox, color, width, true, fill, depth);
        }
    }

    public static void drawShapeAlternative(class_2338 blockPos, class_265 voxelShape, int color, float width, boolean fill, boolean depth) {
        class_243 vec3d = class_243.method_24954((class_2382)blockPos);
        if (ProjectionUtil.canSee(new class_238(blockPos))) {
            int i;
            if (SHAPE_OUTLINES.size() > 1024) {
                SHAPE_OUTLINES.clear();
            }
            class_3545 pair = SHAPE_OUTLINES.computeIfAbsent(voxelShape, shape -> {
                ArrayList lines = new ArrayList();
                shape.method_1104((minX, minY, minZ, maxX, maxY, maxZ) -> lines.add(new Line(null, new class_243(minX, minY, minZ), new class_243(maxX, maxY, maxZ), 0, 0, 0.0f)));
                return new class_3545((Object)shape.method_1090(), lines);
            });
            if (fill) {
                List left = (List)pair.method_15442();
                int lSize = left.size();
                for (i = 0; i < lSize; ++i) {
                    Render3DUtil.drawBox(((class_238)left.get(i)).method_997(vec3d), color, width, false, true, depth);
                }
            }
            List right = (List)pair.method_15441();
            int rSize = right.size();
            for (i = 0; i < rSize; ++i) {
                Line line = (Line)right.get(i);
                Render3DUtil.drawLine(line.start.method_1019(vec3d), line.end.method_1019(vec3d), color, width, depth);
            }
        }
    }

    public static void drawBox(class_238 box, int color, float width) {
        Render3DUtil.drawBox(box, color, width, true, true, false);
    }

    public static void drawBox(class_238 box, int color, float width, boolean line, boolean fill, boolean depth) {
        Render3DUtil.drawBox(null, box, color, width, line, fill, depth);
    }

    public static void drawBox(class_4587.class_4665 entry, class_238 box, int color, float width, boolean line, boolean fill, boolean depth) {
        box = box.method_1014(0.001);
        double x1 = box.field_1323;
        double y1 = box.field_1322;
        double z1 = box.field_1321;
        double x2 = box.field_1320;
        double y2 = box.field_1325;
        double z2 = box.field_1324;
        class_243 p000 = new class_243(x1, y1, z1);
        class_243 p100 = new class_243(x2, y1, z1);
        class_243 p101 = new class_243(x2, y1, z2);
        class_243 p001 = new class_243(x1, y1, z2);
        class_243 p010 = new class_243(x1, y2, z1);
        class_243 p110 = new class_243(x2, y2, z1);
        class_243 p111 = new class_243(x2, y2, z2);
        class_243 p011 = new class_243(x1, y2, z2);
        if (fill) {
            int fillColor = ColorUtil.multAlpha(color, 0.3f);
            Render3DUtil.drawQuad(entry, p000, p100, p101, p001, fillColor, depth);
            Render3DUtil.drawQuad(entry, p000, p010, p110, p100, fillColor, depth);
            Render3DUtil.drawQuad(entry, p100, p110, p111, p101, fillColor, depth);
            Render3DUtil.drawQuad(entry, p001, p101, p111, p011, fillColor, depth);
            Render3DUtil.drawQuad(entry, p000, p001, p011, p010, fillColor, depth);
            Render3DUtil.drawQuad(entry, p010, p011, p111, p110, fillColor, depth);
        }
        if (line) {
            Render3DUtil.drawLine(entry, p000, p100, color, color, width, depth);
            Render3DUtil.drawLine(entry, p100, p101, color, color, width, depth);
            Render3DUtil.drawLine(entry, p101, p001, color, color, width, depth);
            Render3DUtil.drawLine(entry, p001, p000, color, color, width, depth);
            Render3DUtil.drawLine(entry, p001, p011, color, color, width, depth);
            Render3DUtil.drawLine(entry, p000, p010, color, color, width, depth);
            Render3DUtil.drawLine(entry, p101, p111, color, color, width, depth);
            Render3DUtil.drawLine(entry, p100, p110, color, color, width, depth);
            Render3DUtil.drawLine(entry, p010, p110, color, color, width, depth);
            Render3DUtil.drawLine(entry, p110, p111, color, color, width, depth);
            Render3DUtil.drawLine(entry, p111, p011, color, color, width, depth);
            Render3DUtil.drawLine(entry, p011, p010, color, color, width, depth);
        }
    }

    public static void vertexLine(class_4587 matrices, class_4588 buffer, class_243 start, class_243 end, int startColor, int endColor) {
        class_243 cameraPos = Render3DUtil.mc.field_1773.method_19418().method_71156();
        start = start.method_1020(cameraPos);
        end = end.method_1020(cameraPos);
        Render3DUtil.vertexLine(matrices.method_23760(), buffer, start.method_46409(), end.method_46409(), startColor, endColor);
    }

    public static void vertexLine(class_4587.class_4665 entry, class_4588 buffer, class_243 start, class_243 end, int startColor, int endColor) {
        if (entry == null) {
            entry = lastWorldSpaceMatrix;
            class_243 cameraPos = Render3DUtil.mc.field_1773.method_19418().method_71156();
            start = start.method_1020(cameraPos);
            end = end.method_1020(cameraPos);
        }
        Render3DUtil.vertexLine(entry, buffer, start.method_46409(), end.method_46409(), startColor, endColor);
    }

    public static void vertexLine(class_4587.class_4665 entry, class_4588 buffer, Vector3f start, Vector3f end, int startColor, int endColor) {
        if (entry == null) {
            entry = lastWorldSpaceMatrix;
        }
        Vector3f vec = Render3DUtil.getNormal(start, end);
        buffer.method_61032(entry, start).method_39415(startColor).method_61959(entry, vec);
        buffer.method_61032(entry, end).method_39415(endColor).method_61959(entry, vec);
    }

    private static void vertexDebugLine(class_4587.class_4665 entry, class_4588 buffer, class_243 start, class_243 end, int startColor, int endColor) {
        class_243 origin = class_243.field_1353;
        if (entry == null) {
            entry = lastWorldSpaceMatrix;
            origin = Render3DUtil.mc.field_1773.method_19418().method_71156();
        }
        Render3DUtil.vertexPosition(entry, buffer, start, origin, startColor);
        Render3DUtil.vertexPosition(entry, buffer, end, origin, endColor);
    }

    private static void vertexFlat(class_4587.class_4665 entry, class_4588 buffer, class_243 pos, int color) {
        if (entry == null) {
            entry = lastWorldSpaceMatrix;
            pos = pos.method_1020(Render3DUtil.mc.field_1773.method_19418().method_71156());
        }
        buffer.method_61032(entry, pos.method_46409()).method_39415(color);
    }

    public static void vertexQuad(class_4587.class_4665 entry, class_4588 buffer, class_243 vec1, class_243 vec2, class_243 vec3, class_243 vec4, int color) {
        class_243 origin = class_243.field_1353;
        if (entry == null) {
            entry = lastWorldSpaceMatrix;
            origin = Render3DUtil.mc.field_1773.method_19418().method_71156();
        }
        Render3DUtil.vertexPosition(entry, buffer, vec1, origin, color);
        Render3DUtil.vertexPosition(entry, buffer, vec2, origin, color);
        Render3DUtil.vertexPosition(entry, buffer, vec3, origin, color);
        Render3DUtil.vertexPosition(entry, buffer, vec4, origin, color);
    }

    private static void vertexPosition(class_4587.class_4665 entry, class_4588 buffer, class_243 position, class_243 origin, int color) {
        buffer.method_56824(entry, (float)(position.field_1352 - origin.field_1352), (float)(position.field_1351 - origin.field_1351), (float)(position.field_1350 - origin.field_1350)).method_39415(color);
    }

    public static void vertexQuad(class_4587.class_4665 entry, class_4588 buffer, Vector3f vec1, Vector3f vec2, Vector3f vec3, Vector3f vec4, int color) {
        if (entry == null) {
            entry = lastWorldSpaceMatrix;
        }
        buffer.method_61032(entry, vec1).method_39415(color);
        buffer.method_61032(entry, vec2).method_39415(color);
        buffer.method_61032(entry, vec3).method_39415(color);
        buffer.method_61032(entry, vec4).method_39415(color);
    }

    private static void vertexGradientQuad(class_4587.class_4665 entry, class_4588 buffer, class_243 vec1, class_243 vec2, class_243 vec3, class_243 vec4, int color1, int color2, int color3, int color4) {
        if (entry == null) {
            entry = lastWorldSpaceMatrix;
            class_243 cameraPos = Render3DUtil.mc.field_1773.method_19418().method_71156();
            vec1 = vec1.method_1020(cameraPos);
            vec2 = vec2.method_1020(cameraPos);
            vec3 = vec3.method_1020(cameraPos);
            vec4 = vec4.method_1020(cameraPos);
        }
        buffer.method_61032(entry, vec1.method_46409()).method_39415(color1);
        buffer.method_61032(entry, vec2.method_46409()).method_39415(color2);
        buffer.method_61032(entry, vec3.method_46409()).method_39415(color3);
        buffer.method_61032(entry, vec4.method_46409()).method_39415(color4);
    }

    public static void quadTexture(class_4587.class_4665 entry, class_287 buffer, float x, float y, float width, float height, Vector4i color) {
        buffer.method_56824(entry, x, y + height, 0.0f).method_22913(0.0f, 0.0f).method_39415(color.x);
        buffer.method_56824(entry, x + width, y + height, 0.0f).method_22913(0.0f, 1.0f).method_39415(color.y);
        buffer.method_56824(entry, x + width, y, 0.0f).method_22913(1.0f, 1.0f).method_39415(color.w);
        buffer.method_56824(entry, x, y, 0.0f).method_22913(1.0f, 0.0f).method_39415(color.z);
    }

    @NotNull
    public static Vector3f getNormal(Vector3f start, Vector3f end) {
        Vector3f normal = new Vector3f((Vector3fc)start).sub((Vector3fc)end);
        float sqrt = class_3532.method_15355((float)normal.lengthSquared());
        return normal.div(sqrt);
    }

    public static void drawCube(class_4587 matrices, class_1309 lastTarget, float anim, float red, float scale, int glowPasses) {
        float fade = Render3DUtil.smoothTargetFade(anim);
        float size = 1.2f * scale;
        class_4184 camera = Render3DUtil.mc.field_1773.method_19418();
        class_243 vec = MathUtil.interpolate((class_1297)lastTarget).method_1020(camera.method_71156());
        matrices.method_22903();
        matrices.method_22904(vec.field_1352, vec.field_1351 + lastTarget.method_5829().method_17940() / 1.8, vec.field_1350);
        matrices.method_22907((Quaternionfc)camera.method_23767());
        matrices.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(MathUtil.interpolate(prevEspValue, espValue)));
        class_4587.class_4665 entry = matrices.method_23760().method_56822();
        Vector4i baseColor = ColorUtil.multDark(ColorUtil.multRedAndAlpha(new Vector4i(ColorUtil.fade(90), ColorUtil.fade(0), ColorUtil.fade(180), ColorUtil.fade(270)), 1.0f + red * 10.0f, fade), fade);
        Vector4i glowColor = ColorUtil.multDark(ColorUtil.multRedAndAlpha(new Vector4i(ColorUtil.fade(90), ColorUtil.fade(0), ColorUtil.fade(180), ColorUtil.fade(270)), 1.0f + red * 10.0f, 0.34f * fade), fade);
        for (int i = 0; i < glowPasses; ++i) {
            Render3DUtil.drawTextureAdditive(entry, captureId, -size / 2.0f, -size / 2.0f, size, size, glowColor, false);
        }
        Render3DUtil.drawTexture(entry, captureId, -size / 2.0f, -size / 2.0f, size, size, baseColor, false);
        matrices.method_22909();
    }

    public static void drawCube2(class_4587 matrices, class_1309 lastTarget, float anim, float red, float scale, int glowPasses) {
        float fade = Render3DUtil.smoothTargetFade(anim);
        float size = 1.1f * scale;
        class_4184 camera = Render3DUtil.mc.field_1773.method_19418();
        class_243 vec = MathUtil.interpolate((class_1297)lastTarget).method_1020(camera.method_71156());
        matrices.method_22903();
        matrices.method_22904(vec.field_1352, vec.field_1351 + lastTarget.method_5829().method_17940() / 1.8, vec.field_1350);
        matrices.method_22907((Quaternionfc)camera.method_23767());
        matrices.method_22907((Quaternionfc)class_7833.field_40718.rotationDegrees(MathUtil.interpolate(prevEspValue, espValue)));
        class_4587.class_4665 entry = matrices.method_23760().method_56822();
        Vector4i baseColor = ColorUtil.multDark(ColorUtil.multRedAndAlpha(new Vector4i(ColorUtil.fade(90), ColorUtil.fade(0), ColorUtil.fade(180), ColorUtil.fade(270)), 1.0f + red * 10.0f, fade), fade);
        Vector4i glowColor = ColorUtil.multDark(ColorUtil.multRedAndAlpha(new Vector4i(ColorUtil.fade(90), ColorUtil.fade(0), ColorUtil.fade(180), ColorUtil.fade(270)), 1.0f + red * 10.0f, 0.38f * fade), fade);
        for (int i = 0; i < glowPasses; ++i) {
            Render3DUtil.drawTextureAdditive(entry, targetN2, -size / 2.0f, -size / 2.0f, size, size, glowColor, false);
        }
        Render3DUtil.drawTextureAdditive(entry, targetN2, -size / 2.0f, -size / 2.0f, size, size, baseColor, false);
        matrices.method_22909();
    }

    public static void drawGhosts(class_4587 matrices, class_1309 lastTarget, float anim, float red, float speed) {
        class_4184 camera = Render3DUtil.mc.field_1773.method_19418();
        class_243 vec = MathUtil.interpolate((class_1297)lastTarget).method_1020(camera.method_71156());
        double iAge = MathUtil.interpolate(Render3DUtil.mc.field_1724.field_6012 - 1, Render3DUtil.mc.field_1724.field_6012);
        float halfHeight = lastTarget.method_17682() / 2.0f + 0.1f;
        float width = lastTarget.method_17681();
        float fade = Render3DUtil.smoothTargetFade(anim);
        Vector4i[] colors = new Vector4i[11];
        for (int i = 0; i <= 10; ++i) {
            float offset = ((float)i + 10.0f) / 20.0f;
            int color = ColorUtil.multDark(ColorUtil.multRedAndAlpha(ColorUtil.fade((int)(offset * 180.0f)), 1.0f + red * 10.0f, offset * fade), fade);
            colors[i] = new Vector4i(color);
        }
        for (int j = 0; j < 3; ++j) {
            int length = 10;
            for (int i = 0; i <= length; ++i) {
                double radians = Math.toRadians((((double)((float)i / 2.0f) + iAge * (double)speed) * (double)length + (double)(j * 120)) % (double)(length * 360));
                double sinQuad = Math.sin(Math.toRadians(iAge * 2.5 * (double)speed + (double)((float)i * ((float)j + halfHeight))) * 2.0) / 2.0;
                float offset = (float)(i + length) / (float)(length + length);
                matrices.method_22903();
                matrices.method_22904(vec.field_1352 + Math.cos(radians) * (double)width, vec.field_1351 + (double)halfHeight + sinQuad, vec.field_1350 + Math.sin(radians) * (double)width);
                matrices.method_22907((Quaternionfc)camera.method_23767());
                class_4587.class_4665 entry = matrices.method_23760().method_56822();
                float scale = 0.5f * offset;
                Render3DUtil.drawTexture(entry, bloom, -scale / 2.0f, -scale / 2.0f, scale, scale, colors[i], false);
                matrices.method_22909();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void drawGhosts3(class_4587 matrices, class_1309 target, float anim, float speed) {
        if (matrices == null || target == null || anim <= 0.0f) {
            return;
        }
        class_4184 camera = Render3DUtil.mc.field_1773.method_19418();
        class_243 cameraPos = camera.method_71156();
        class_243 targetPos = MathUtil.interpolate((class_1297)target);
        double time = (double)System.currentTimeMillis() / 1000.0 * (double)speed;
        double rotation = time * 3.0 % (Math.PI * 2);
        double radius = target.method_17681();
        double baseY = target.method_17682() / 2.0f + 0.2f;
        int alpha = (int)(255.0f * class_3532.method_15363((float)anim, (float)0.0f, (float)1.0f));
        int segments = 14;
        double arcStep = Math.toRadians(60.0) / (double)segments;
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShaderTexture(0, glow);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        class_287 builder = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        try {
            for (int orbit = 0; orbit < 4; ++orbit) {
                double timeOffset = time + (double)orbit * 15.0;
                double orbitOffset = (double)orbit * 1.5707963267948966;
                for (int segment = 0; segment <= segments; ++segment) {
                    double arcAngle = (double)segment * arcStep;
                    double angle = arcAngle + rotation + orbitOffset;
                    double x = targetPos.field_1352 + radius * Math.cos(angle);
                    double y = targetPos.field_1351 + Math.sin(timeOffset + arcAngle + (double)orbit) * 0.7 + baseY;
                    double z = targetPos.field_1350 + radius * Math.sin(angle);
                    float size = 0.4f * (0.5f + (float)segment / (float)segments);
                    int color = ColorUtil.replAlpha(ColorUtil.fade(segment * segments), alpha);
                    matrices.method_22903();
                    matrices.method_22904(x - cameraPos.field_1352, y - cameraPos.field_1351, z - cameraPos.field_1350);
                    matrices.method_22907((Quaternionfc)camera.method_23767());
                    matrices.method_22905(size, size, size);
                    Render3DUtil.drawImmediateImage(matrices, builder, -0.5, -0.5, 0.0, 1.0, 1.0, color);
                    matrices.method_22909();
                }
            }
            BufferRenderer.drawWithGlobalProgram(builder.method_60800());
        }
        finally {
            LegacyRenderSystem.depthMask(true);
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.defaultBlendFunc();
            LegacyRenderSystem.disableBlend();
        }
    }

    public static void drawGhosts2(class_4587 matrices, class_1309 target, float anim, float red, float speed) {
        class_4184 camera = Render3DUtil.mc.field_1773.method_19418();
        class_243 center = MathUtil.interpolate((class_1297)target).method_1020(camera.method_71156()).method_1031(0.0, (double)target.method_17682() / 2.0, 0.0);
        float fade = Render3DUtil.easeOutCubic(Render3DUtil.smoothTargetFade(anim));
        if (fade <= 0.01f) {
            return;
        }
        double time = (double)System.currentTimeMillis() * 0.001 * (double)speed;
        double height = target.method_17682();
        int theme = ColorUtil.multRed(ColorUtil.getClientColor(), 1.0f + red * 10.0f);
        int segments = 47;
        double tailSpan = 1.8849555921538759;
        double headCut = 1.0 - (double)fade;
        float baseScale = 0.16f * Math.max(0.2f, fade);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShaderTexture(0, bloom);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        class_287 builder = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        for (int orbit = 0; orbit < 3; ++orbit) {
            double yOffset = switch (orbit) {
                case 0 -> -height * 0.35 + 0.15;
                case 2 -> height * 0.45 - 0.15;
                default -> 0.0;
            };
            double headAngle = (time + GHOST_PHASES[orbit]) * Math.PI * 2.0;
            for (int segment = 0; segment < segments; ++segment) {
                double s = (double)segment / (double)(segments - 1);
                if (s < headCut - 0.002) continue;
                double reveal = class_3532.method_15350((double)((s - (headCut - 0.002)) / 0.002), (double)0.0, (double)1.0);
                double strength = 1.0 - s;
                double angle = headAngle - s * tailSpan;
                double radius = GHOST_RADII[orbit] * (1.0 - 0.06 * s) + 0.1 * Math.sin(angle * 1.45 + (double)orbit);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = yOffset + 0.12 * Math.sin(angle * 1.25 + (double)orbit * 0.6);
                float headSize = orbit == 1 ? 1.1f : 1.0f;
                float size = (float)((double)baseScale * (0.002 + ((double)headSize - 0.002) * GHOST_STRENGTH_SIZE[segment]));
                float whiteAlpha = Math.min(1.0f, (float)(1.05 * (double)fade * GHOST_STRENGTH_WHITE[segment] * reveal));
                float colorAlpha = Math.min(0.55f, (float)(1.1 * (double)fade * GHOST_STRENGTH_COLOR[segment] * reveal));
                if (whiteAlpha < 0.001f && colorAlpha < 0.001f) continue;
                matrices.method_22903();
                matrices.method_22904(center.field_1352 + x, center.field_1351 + y, center.field_1350 + z);
                matrices.method_22907((Quaternionfc)camera.method_23767());
                float whiteSize = size * 0.8f;
                float haloSize = size * 1.34f;
                Render3DUtil.drawImmediateImage(matrices, builder, -whiteSize, -whiteSize, 0.0, whiteSize * 2.0f, whiteSize * 2.0f, ColorUtil.getColor(255, 255, 255, Math.round(whiteAlpha * 255.0f)));
                Render3DUtil.drawImmediateImage(matrices, builder, -haloSize, -haloSize, 0.0, haloSize * 2.0f, haloSize * 2.0f, ColorUtil.replAlpha(theme, colorAlpha));
                matrices.method_22909();
            }
        }
        BufferRenderer.drawWithGlobalProgram(builder.method_60800());
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
    }

    public static void drawGlowRing(class_4587 matrices, class_1309 lastTarget, float anim, float red) {
        class_4184 camera = Render3DUtil.mc.field_1773.method_19418();
        class_243 vec = MathUtil.interpolate((class_1297)lastTarget).method_1020(camera.method_71156());
        float fade = Render3DUtil.easeOutCubic(Render3DUtil.smoothTargetFade(anim));
        float radius = lastTarget.method_17681() * 0.7f;
        float height = lastTarget.method_17682();
        double step = MathUtil.interpolate(circleStep - 0.15f, circleStep);
        double headY = Math.abs(Math.sin(step)) * (double)height;
        double tailY = Math.abs(Math.sin(step - 0.4)) * (double)height;
        int color = ColorUtil.multRed(ColorUtil.getClientColor(), 1.0f + red * 10.0f);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShaderTexture(0, bloom);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        class_287 builder = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        int totalPoints = 80;
        int tailSegments = 6;
        float headSize = 0.12f;
        float tailSize = 0.075f;
        for (int i = 0; i < totalPoints; ++i) {
            double angle = Math.PI * 2 * (double)i / (double)totalPoints;
            float x = (float)(Math.cos(angle) * (double)radius);
            float z = (float)(Math.sin(angle) * (double)radius);
            matrices.method_22903();
            matrices.method_22904(vec.field_1352 + (double)x, vec.field_1351 + headY, vec.field_1350 + (double)z);
            matrices.method_22907((Quaternionfc)camera.method_23767());
            Render3DUtil.drawImmediateImage(matrices, builder, (double)(-headSize) / 2.0, (double)(-headSize) / 2.0, 0.0, headSize, headSize, ColorUtil.multAlpha(color, fade));
            matrices.method_22909();
            for (int tail = 1; tail <= tailSegments; ++tail) {
                float progress = (float)tail / (float)(tailSegments + 1);
                double currentY = headY + (tailY - headY) * (double)progress;
                float alpha = fade * (1.0f - progress) * 0.78f;
                matrices.method_22903();
                matrices.method_22904(vec.field_1352 + (double)x, vec.field_1351 + currentY, vec.field_1350 + (double)z);
                matrices.method_22907((Quaternionfc)camera.method_23767());
                Render3DUtil.drawImmediateImage(matrices, builder, (double)(-tailSize) / 2.0, (double)(-tailSize) / 2.0, 0.0, tailSize, tailSize, ColorUtil.multAlpha(color, alpha));
                matrices.method_22909();
            }
        }
        BufferRenderer.drawWithGlobalProgram(builder.method_60800());
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
    }

    private static void renderTargetCrystal(class_4587 matrices, class_287 buffer, float x, float y, float z, float size, int color) {
        matrices.method_22903();
        matrices.method_46416(x, y, z);
        matrices.method_22905(size, size, size);
        Matrix4f matrix = matrices.method_23760().method_23761();
        for (int i = 0; i < CRYSTAL_FACES.length; ++i) {
            int[] face = CRYSTAL_FACES[i];
            int shaded = Render3DUtil.applyBrightness(color, CRYSTAL_BRIGHTNESS[i]);
            Vector3f v1 = CRYSTAL_VERTICES[face[0]];
            Vector3f v2 = CRYSTAL_VERTICES[face[1]];
            Vector3f v3 = CRYSTAL_VERTICES[face[2]];
            buffer.method_22918((Matrix4fc)matrix, v1.x, v1.y, v1.z).method_39415(shaded);
            buffer.method_22918((Matrix4fc)matrix, v2.x, v2.y, v2.z).method_39415(shaded);
            buffer.method_22918((Matrix4fc)matrix, v3.x, v3.y, v3.z).method_39415(shaded);
        }
        matrices.method_22909();
    }

    private static int applyBrightness(int color, float brightness) {
        int alpha = ColorUtil.alpha(color);
        int red = Math.min(255, Math.max(0, (int)((float)ColorUtil.red(color) * brightness)));
        int green = Math.min(255, Math.max(0, (int)((float)ColorUtil.green(color) * brightness)));
        int blue = Math.min(255, Math.max(0, (int)((float)ColorUtil.blue(color) * brightness)));
        return ColorUtil.getColor(red, green, blue, alpha);
    }

    private static float smoothTargetFade(float anim) {
        float fade = class_3532.method_15363((float)anim, (float)0.0f, (float)1.0f);
        return fade * fade * (3.0f - 2.0f * fade);
    }

    private static float easeOutCubic(float x) {
        return 1.0f - (float)Math.pow(1.0f - class_3532.method_15363((float)x, (float)0.0f, (float)1.0f), 3.0);
    }

    private static void drawImmediateImage(class_4587 matrices, class_287 builder, double x, double y, double z, double width, double height, int color) {
        Matrix4f matrix = matrices.method_23760().method_23761();
        builder.method_22918((Matrix4fc)matrix, (float)x, (float)(y + height), (float)z).method_22913(0.0f, 1.0f).method_39415(color);
        builder.method_22918((Matrix4fc)matrix, (float)(x + width), (float)(y + height), (float)z).method_22913(1.0f, 1.0f).method_39415(color);
        builder.method_22918((Matrix4fc)matrix, (float)(x + width), (float)y, (float)z).method_22913(1.0f, 0.0f).method_39415(color);
        builder.method_22918((Matrix4fc)matrix, (float)x, (float)y, (float)z).method_22913(0.0f, 0.0f).method_39415(color);
    }

    public static void updateTargetEsp() {
        if (Render3DUtil.mc.field_1724 != null) {
            if (targetEspUpdateAge == Render3DUtil.mc.field_1724.field_6012) {
                return;
            }
            targetEspUpdateAge = Render3DUtil.mc.field_1724.field_6012;
        }
        prevEspValue = espValue;
        prevCrystalEspValue = crystalEspValue;
        espValue += espSpeed;
        crystalEspValue += 8.0f;
        if (espSpeed > 25.0f) {
            flipSpeed = true;
        }
        if (espSpeed < -25.0f) {
            flipSpeed = false;
        }
        espSpeed = flipSpeed ? espSpeed - 0.5f : espSpeed + 0.5f;
        circleStep += 0.15f;
    }

    public static void resetCircleSmoothing() {
        circleSmoothY = 0.0;
        circleSmoothY2 = 0.0;
        crystalCircleY = Double.NaN;
        crystalCircleOffset = 0.0;
        crystalCircleFrameTime = 0L;
        circleHitValue = 0.0f;
        blackFlashLastTime = 0L;
    }

    public static void drawLine(class_4587.class_4665 entry, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float width, boolean depth) {
        Render3DUtil.drawLine(entry, new class_243(minX, minY, minZ), new class_243(maxX, maxY, maxZ), color, color, width, depth);
    }

    public static void drawLine(class_243 start, class_243 end, int color, float width, boolean depth) {
        Render3DUtil.drawLine(null, start, end, color, color, width, depth);
    }

    public static void drawLine(class_4587.class_4665 entry, class_243 start, class_243 end, int colorStart, int colorEnd, float width, boolean depth) {
        Line line = new Line(entry, start, end, colorStart, colorEnd, width);
        if (depth) {
            LINE_DEPTH.add(line);
        } else {
            LINE.add(line);
        }
    }

    public static void drawLineGradient(class_243 start, class_243 end, int colorStart, int colorEnd, float width, boolean depth) {
        Render3DUtil.drawLine(null, start, end, colorStart, colorEnd, width, depth);
    }

    private static void renderLightningSegments(List<LightningSegment> segments, boolean depth) {
        if (segments.isEmpty()) {
            return;
        }
        Shader shader = Render3DUtil.getLightningShader();
        if (shader == null) {
            segments.clear();
            return;
        }
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.disableCull();
        if (depth) {
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.depthFunc(515);
            LegacyRenderSystem.depthMask(false);
        } else {
            LegacyRenderSystem.disableDepthTest();
        }
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        shader.bind();
        shader.setUniformMatrix4f("ModelViewMat", lastWorldSpaceMatrix.method_23761());
        shader.setUniformMatrix4f("ProjMat", lastProjMat);
        shader.setUniform1f("Time", (float)System.nanoTime() / 1.0E9f);
        LinkedHashMap<LightningBatchKey, List> batches = new LinkedHashMap<LightningBatchKey, List>();
        for (LightningSegment lightningSegment : segments) {
            batches.computeIfAbsent(new LightningBatchKey(lightningSegment.color, lightningSegment.coreStrength), ignored -> new ArrayList()).add(lightningSegment);
        }
        for (Map.Entry entry : batches.entrySet()) {
            LightningBatchKey key = (LightningBatchKey)entry.getKey();
            int color = key.color;
            shader.bind();
            shader.setUniform4f("Color", (float)ColorUtil.red(color) / 255.0f, (float)ColorUtil.green(color) / 255.0f, (float)ColorUtil.blue(color) / 255.0f, (float)ColorUtil.alpha(color) / 255.0f);
            shader.setUniform1f("CoreStrength", key.coreStrength);
            class_287 buffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1592);
            ((List<LightningSegment>)entry.getValue()).forEach(segment -> Render3DUtil.vertexLightningQuad(buffer, segment));
            shader.bind();
            BufferRenderer.draw(buffer.method_60800());
        }
        shader.unbind();
        if (depth) {
            LegacyRenderSystem.depthMask(true);
            LegacyRenderSystem.depthFunc(513);
        } else {
            LegacyRenderSystem.enableDepthTest();
        }
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.disableBlend();
        segments.clear();
    }

    private static Shader getLightningShader() {
        if (lightningShader != null || lightningShaderFailed) {
            return lightningShader;
        }
        try {
            lightningShader = new Shader("targetesp", "lightning");
        }
        catch (RuntimeException exception) {
            lightningShaderFailed = true;
        }
        return lightningShader;
    }

    private static void vertexLightningQuad(class_287 buffer, LightningSegment segment) {
        class_243 view;
        class_243 cameraPos = Render3DUtil.mc.field_1773.method_19418().method_71156();
        class_243 middle = segment.start.method_1019(segment.end).method_1021(0.5);
        class_243 direction = segment.end.method_1020(segment.start).method_1029();
        class_243 side = direction.method_1036(view = cameraPos.method_1020(middle).method_1029());
        if (side.method_1027() < 1.0E-6) {
            side = new class_243(0.0, 1.0, 0.0);
        }
        side = side.method_1029().method_1021((double)segment.width);
        buffer.method_60830((Vector3fc)segment.start.method_1020(side).method_1020(cameraPos).method_46409());
        buffer.method_60830((Vector3fc)segment.start.method_1019(side).method_1020(cameraPos).method_46409());
        buffer.method_60830((Vector3fc)segment.end.method_1019(side).method_1020(cameraPos).method_46409());
        buffer.method_60830((Vector3fc)segment.end.method_1020(side).method_1020(cameraPos).method_46409());
    }

    public static void drawAdditiveLineGradient(class_243 start, class_243 end, int colorStart, int colorEnd, float width, boolean depth) {
        Line line = new Line(null, start, end, colorStart, colorEnd, width);
        if (depth) {
            ADDITIVE_LINE_DEPTH.add(line);
        } else {
            ADDITIVE_LINE.add(line);
        }
    }

    public static void drawQuad(class_243 x, class_243 y, class_243 w, class_243 z, int color, boolean depth) {
        Render3DUtil.drawQuad(null, x, y, w, z, color, depth);
    }

    public static void drawGradientQuad(class_243 x, class_243 y, class_243 w, class_243 z, int colorX, int colorY, int colorW, int colorZ, boolean depth) {
        GradientQuad quad = new GradientQuad(null, x, y, w, z, colorX, colorY, colorW, colorZ);
        if (depth) {
            GRADIENT_QUAD_DEPTH.add(quad);
        } else {
            GRADIENT_QUAD.add(quad);
        }
    }

    public static void drawFOV(class_4587 stack, class_1657 player, float range, float fov, int color) {
        if (fov <= 0.0f) {
            return;
        }
        float radius = (float)((double)range * Math.tan(Math.toRadians((double)fov / 2.0)));
        class_243 lookVec = player.method_5828(1.0f).method_1021((double)range);
        class_243 center = player.method_33571().method_1019(lookVec);
        class_243 up = new class_243(0.0, 1.0, 0.0);
        class_243 right = lookVec.method_1036(up).method_1029();
        if (right.method_1027() == 0.0) {
            right = new class_243(1.0, 0.0, 0.0);
        }
        up = right.method_1036(lookVec).method_1029();
        int segments = 64;
        for (int i = 0; i < segments; ++i) {
            double angle = Math.toRadians((double)i * 360.0 / (double)segments);
            double nextAngle = Math.toRadians((double)(i + 1) * 360.0 / (double)segments);
            class_243 p1 = center.method_1019(right.method_1021(Math.cos(angle) * (double)radius)).method_1019(up.method_1021(Math.sin(angle) * (double)radius));
            class_243 p2 = center.method_1019(right.method_1021(Math.cos(nextAngle) * (double)radius)).method_1019(up.method_1021(Math.sin(nextAngle) * (double)radius));
            Render3DUtil.drawLine(p1, p2, color, 2.0f, true);
        }
    }

    public static void drawQuad(class_4587.class_4665 entry, class_243 x, class_243 y, class_243 w, class_243 z, int color, boolean depth) {
        Quad quad = new Quad(entry, x, y, w, z, color);
        if (depth) {
            QUAD_DEPTH.add(quad);
        } else {
            QUAD.add(quad);
        }
    }

    public static void drawCircle(class_4587 matrix, class_1309 targetEntity, float anim, float red, int baseColor1, int baseColor2) {
        double cs = MathUtil.interpolate(circleStep - 0.17f, circleStep);
        class_243 target = MathUtil.interpolate((class_1297)targetEntity);
        boolean canSee = false;
        circleHitValue = 0.0f;
        float hitEffect = 0.0f;
        float distanceMultiplier = 0.84f;
        int size = 240;
        float entityWidth = targetEntity.method_17681() * distanceMultiplier;
        float entityHeight = targetEntity.method_17682();
        double targetY = Render3DUtil.smoothCircleSin(cs) * (double)entityHeight;
        double targetY2 = Render3DUtil.smoothCircleSin(cs - 0.35) * (double)entityHeight;
        circleSmoothY = Render3DUtil.lerp(circleSmoothY, targetY, 0.15);
        circleSmoothY2 = Render3DUtil.lerp(circleSmoothY2, targetY2, 0.13);
        float hitTint = hitEffect * hitEffect * (3.0f - 2.0f * hitEffect);
        int hitRed = ColorUtil.getColor(255, 62, 70, 255);
        int color1 = ColorUtil.overCol(baseColor1, hitRed, hitTint);
        int color2 = ColorUtil.overCol(baseColor2, hitRed, hitTint);
        for (int i = 0; i < size; ++i) {
            float t = (float)i / (float)size;
            float tNext = (float)((i + 1) % size) / (float)size;
            float gradientT = (float)(0.5 - 0.5 * Math.cos((double)t * Math.PI * 2.0));
            float gradientTNext = (float)(0.5 - 0.5 * Math.cos((double)tNext * Math.PI * 2.0));
            int currentColor = ColorUtil.overCol(color1, color2, gradientT);
            int nextColor = ColorUtil.overCol(color1, color2, gradientTNext);
            int brightCurrentColor = ColorUtil.multBright(currentColor, 0.96f);
            int brightNextColor = ColorUtil.multBright(nextColor, 0.96f);
            float movingAlpha = 0.7445f * anim;
            float middleAlpha = movingAlpha * 0.5f;
            int movingColor = ColorUtil.multAlpha(brightCurrentColor, movingAlpha);
            int movingColorNext = ColorUtil.multAlpha(brightNextColor, movingAlpha);
            int middleColor = ColorUtil.multAlpha(brightCurrentColor, middleAlpha);
            int middleColorNext = ColorUtil.multAlpha(brightNextColor, middleAlpha);
            int farColor = ColorUtil.multAlpha(brightCurrentColor, 0.0f);
            int farColorNext = ColorUtil.multAlpha(brightNextColor, 0.0f);
            class_243 cosSin = MathUtil.cosSin(i, size, entityWidth);
            class_243 nextCosSin = MathUtil.cosSin((i + 1) % size, size, entityWidth);
            class_243 circlePoint = target.method_1031(cosSin.field_1352, circleSmoothY, cosSin.field_1350);
            class_243 trailPoint = target.method_1031(cosSin.field_1352, circleSmoothY2, cosSin.field_1350);
            class_243 nextCirclePoint = target.method_1031(nextCosSin.field_1352, circleSmoothY, nextCosSin.field_1350);
            class_243 nextTrailPoint = target.method_1031(nextCosSin.field_1352, circleSmoothY2, nextCosSin.field_1350);
            class_243 middlePoint = circlePoint.method_35590(trailPoint, 0.5);
            class_243 nextMiddlePoint = nextCirclePoint.method_35590(nextTrailPoint, 0.5);
            Render3DUtil.drawGradientQuad(circlePoint, nextCirclePoint, nextMiddlePoint, middlePoint, movingColor, movingColorNext, middleColorNext, middleColor, canSee);
            Render3DUtil.drawGradientQuad(middlePoint, nextMiddlePoint, nextTrailPoint, trailPoint, middleColor, middleColorNext, farColorNext, farColor, canSee);
            int outlineColor = ColorUtil.multAlpha(-1, movingAlpha);
            int outlineColorNext = ColorUtil.multAlpha(-1, movingAlpha);
            Render3DUtil.drawLineGradient(circlePoint, nextCirclePoint, outlineColor, outlineColorNext, 1.27f, canSee);
        }
    }

    public static void drawCrystals(class_4587 matrices, class_1309 target, float anim) {
        if (target == null || anim <= 0.0f) {
            return;
        }
        class_4184 camera = Render3DUtil.mc.field_1773.method_19418();
        class_243 targetPos = MathUtil.interpolate((class_1297)target);
        class_243 renderPos = targetPos.method_1020(camera.method_71156());
        float moving = (float)(System.currentTimeMillis() % 360000L) / 2.5f + anim;
        float radius = target.method_17681() * 1.5f;
        float scale = 1.25f - 0.5f * anim;
        int color = ColorUtil.multAlpha(ColorUtil.getClientColor(), anim);
        int bloomColor = ColorUtil.multAlpha(ColorUtil.getClientColor(), anim * 0.2f);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        class_287 crystals = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27379, class_290.field_1576);
        for (int i = 0; i < 360; i += 20) {
            double angle = Math.toRadians((float)i + moving * 0.3f);
            float x = (float)Math.sin(angle) * radius * scale;
            float z = (float)Math.cos(angle) * radius * scale;
            float y = (float)((double)0.1f + (double)target.method_17682() * Math.abs(Math.sin(i)));
            matrices.method_22903();
            class_243 crystalPos = targetPos.method_1031((double)x, (double)y, (double)z);
            class_243 targetCenter = target.method_73189().method_1031(0.0, (double)target.method_17682() / 2.0, 0.0);
            matrices.method_22904(crystalPos.field_1352 - camera.method_71156().field_1352, crystalPos.field_1351 - camera.method_71156().field_1351, crystalPos.field_1350 - camera.method_71156().field_1350);
            Vector3f direction = new Vector3f((float)(targetCenter.field_1352 - crystalPos.field_1352), (float)(targetCenter.field_1351 - crystalPos.field_1351), (float)(targetCenter.field_1350 - crystalPos.field_1350)).normalize();
            matrices.method_22907((Quaternionfc)new Quaternionf().rotationTo((Vector3fc)new Vector3f(0.0f, 1.0f, 0.0f), (Vector3fc)direction));
            Render3DUtil.renderTargetCrystal(matrices, crystals, 0.0f, 0.0f, 0.0f, 0.1f, color);
            matrices.method_22909();
        }
        BufferRenderer.drawWithGlobalProgram(crystals.method_60800());
        LegacyRenderSystem.setShaderTexture(0, glow);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        for (float size : new float[]{1.5f * anim, 0.6f * anim}) {
            class_287 glows = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
            for (int i = 0; i < 360; i += 20) {
                double angle = Math.toRadians((float)i + moving * 0.3f);
                float x = (float)Math.sin(angle) * radius * scale;
                float z = (float)Math.cos(angle) * radius * scale;
                float y = (float)((double)0.1f + (double)target.method_17682() * Math.abs(Math.sin(i)));
                matrices.method_22903();
                matrices.method_22904(renderPos.field_1352 + (double)x, renderPos.field_1351 + (double)y, renderPos.field_1350 + (double)z);
                matrices.method_22907((Quaternionfc)camera.method_23767());
                Render3DUtil.drawImmediateImage(matrices, glows, -size / 2.0f, -size / 2.0f, 0.0, size, size, bloomColor);
                matrices.method_22909();
            }
            BufferRenderer.drawWithGlobalProgram(glows.method_60800());
        }
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.enableCull();
    }

    public static void drawCrystalCircle(class_4587 matrices, class_1309 target, float anim, int color) {
        if (target == null || anim <= 0.0f) {
            return;
        }
        class_243 position = MathUtil.interpolate((class_1297)target).method_1020(Render3DUtil.mc.field_1773.method_19418().method_71156());
        double height = (double)target.method_17682() + 0.02;
        double radius = (double)target.method_17681() * 0.8;
        long time = System.currentTimeMillis() % 1750L;
        boolean inverted = time > 875L;
        double progress = (double)time / 875.0;
        double phase = inverted ? progress - 1.0 : 1.0 - progress;
        double eased = phase < 0.5 ? 2.0 * phase * phase : 1.0 - Math.pow(-2.0 * phase + 2.0, 2.0) / 2.0;
        double targetY = height * eased;
        double targetOffset = height * 0.8000001435473696 * Math.min(eased, 1.0 - eased) * (inverted ? -1.0 : 1.0);
        long frameTime = System.nanoTime();
        double frameDelta = crystalCircleFrameTime == 0L ? 0.016666666666666666 : class_3532.method_15350((double)((double)(frameTime - crystalCircleFrameTime) / 1.0E9), (double)0.0, (double)0.05);
        crystalCircleFrameTime = frameTime;
        if (Double.isNaN(crystalCircleY)) {
            crystalCircleY = targetY;
            crystalCircleOffset = targetOffset;
        } else {
            crystalCircleY = Render3DUtil.lerp(crystalCircleY, targetY, 1.0 - Math.exp(-18.0 * frameDelta));
            crystalCircleOffset = Render3DUtil.lerp(crystalCircleOffset, targetOffset, 1.0 - Math.exp(-10.0 * frameDelta));
        }
        double y = position.field_1351 + crystalCircleY;
        double offset = crystalCircleOffset;
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        class_287 strip = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27380, class_290.field_1576);
        Matrix4f matrix = matrices.method_23760().method_23761();
        for (int degree = 0; degree <= 360; ++degree) {
            double angle = Math.toRadians(degree);
            float x = (float)(position.field_1352 + Math.cos(angle) * radius);
            float z = (float)(position.field_1350 + Math.sin(angle) * radius);
            strip.method_22918((Matrix4fc)matrix, x, (float)y, z).method_39415(ColorUtil.multAlpha(color, 0.55f * anim));
            strip.method_22918((Matrix4fc)matrix, x, (float)(y + offset), z).method_39415(ColorUtil.multAlpha(color, 0.0f));
        }
        BufferRenderer.drawWithGlobalProgram(strip.method_60800());
        class_287 rim = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_29344, class_290.field_1576);
        int rimColor = ColorUtil.multAlpha(color, anim);
        for (int degree = 0; degree < 360; ++degree) {
            double a = Math.toRadians(degree);
            double b = Math.toRadians(degree + 1);
            rim.method_22918((Matrix4fc)matrix, (float)(position.field_1352 + Math.cos(a) * radius), (float)y, (float)(position.field_1350 + Math.sin(a) * radius)).method_39415(rimColor);
            rim.method_22918((Matrix4fc)matrix, (float)(position.field_1352 + Math.cos(b) * radius), (float)y, (float)(position.field_1350 + Math.sin(b) * radius)).method_39415(rimColor);
        }
        BufferRenderer.drawWithGlobalProgram(rim.method_60800());
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.enableCull();
    }

    public static void drawJello(class_4587 matrix, class_1309 targetEntity, float anim, float red) {
        if (targetEntity == null || anim <= 0.0f) {
            return;
        }
        class_243 target = MathUtil.interpolate((class_1297)targetEntity);
        double time = (double)System.currentTimeMillis() / 1000.0;
        int segments = 64;
        double radius = Math.max(0.35, (double)targetEntity.method_17681() * 0.85);
        double height = Math.max(0.1, (double)targetEntity.method_17682());
        double waveY = (Math.sin(time * 2.0) + 1.0) * 0.5 * height;
        int hitColor = ColorUtil.getColor(255, 62, 70, 255);
        int baseColor = ColorUtil.overCol(ColorUtil.getClientColor(), hitColor, class_3532.method_15363((float)red, (float)0.0f, (float)1.0f));
        float alpha = 0.72f * class_3532.method_15363((float)anim, (float)0.0f, (float)1.0f);
        for (int index = 0; index < segments; ++index) {
            double progress = (double)index / (double)segments;
            double nextProgress = ((double)index + 1.0) / (double)segments;
            double angle = progress * Math.PI * 2.0;
            double nextAngle = nextProgress * Math.PI * 2.0;
            double wobble = Math.sin(angle * 3.0 + time * 2.5) * 0.035;
            double nextWobble = Math.sin(nextAngle * 3.0 + time * 2.5) * 0.035;
            class_243 outer = target.method_1031(Math.cos(angle) * radius, waveY + wobble, Math.sin(angle) * radius);
            class_243 nextOuter = target.method_1031(Math.cos(nextAngle) * radius, waveY + nextWobble, Math.sin(nextAngle) * radius);
            class_243 inner = target.method_1031(Math.cos(angle) * radius * 0.97, waveY + wobble - 0.035, Math.sin(angle) * radius * 0.97);
            class_243 nextInner = target.method_1031(Math.cos(nextAngle) * radius * 0.97, waveY + nextWobble - 0.035, Math.sin(nextAngle) * radius * 0.97);
            float shade = 0.74f + 0.26f * (float)((Math.sin(angle - time * 1.75) + 1.0) * 0.5);
            float nextShade = 0.74f + 0.26f * (float)((Math.sin(nextAngle - time * 1.75) + 1.0) * 0.5);
            int outerColor = ColorUtil.multAlpha(ColorUtil.multBright(baseColor, shade), alpha);
            int nextOuterColor = ColorUtil.multAlpha(ColorUtil.multBright(baseColor, nextShade), alpha);
            int innerColor = ColorUtil.multAlpha(ColorUtil.multBright(baseColor, shade), alpha * 0.16f);
            int nextInnerColor = ColorUtil.multAlpha(ColorUtil.multBright(baseColor, nextShade), alpha * 0.16f);
            Render3DUtil.drawGradientQuad(outer, nextOuter, nextInner, inner, outerColor, nextOuterColor, nextInnerColor, innerColor, false);
            int outlineColor = ColorUtil.multAlpha(ColorUtil.multBright(baseColor, 1.16f), alpha * 0.92f);
            int nextOutlineColor = ColorUtil.multAlpha(ColorUtil.multBright(baseColor, 1.16f), alpha * 0.92f);
            Render3DUtil.drawLineGradient(outer, nextOuter, outlineColor, nextOutlineColor, 1.15f, false);
        }
    }

    public static void drawLightning(class_4587 matrix, class_1309 targetEntity, float anim, float red) {
        if (Render3DUtil.mc.field_1724 == null) {
            return;
        }
        class_243 target = MathUtil.interpolate((class_1297)targetEntity);
        boolean canSee = Render3DUtil.mc.field_1724.method_6057((class_1297)targetEntity);
        float fade = Render3DUtil.smoothTargetFade(anim);
        float hurt = class_3532.method_15363((float)red, (float)0.0f, (float)1.0f);
        int theme = ColorUtil.getClientColor();
        int hitColor = ColorUtil.getColor(255, 74, 92, 255);
        int boltColor = ColorUtil.overCol(theme, hitColor, hurt * 0.82f);
        int glowColor = ColorUtil.multAlpha(ColorUtil.multBright(boltColor, 0.67f), 0.44f * fade);
        int wideGlowColor = ColorUtil.multAlpha(ColorUtil.multBright(boltColor, 0.58f), 0.38f * fade);
        int coreColor = ColorUtil.multAlpha(ColorUtil.overCol(boltColor, ColorUtil.getColor(255, 255, 255, 255), 0.78f), 0.98f * fade);
        double radius = Math.max(0.34, (double)targetEntity.method_17681() * 0.64);
        double height = Math.max(0.9, (double)targetEntity.method_17682());
        long seedBase = (long)targetEntity.method_5628() * 341873128712L + (long)targetEntity.field_6012;
        for (int bolt = 0; bolt < 8; ++bolt) {
            long seed = seedBase + (long)bolt * 132897987541L;
            if (Render3DUtil.lightningNoise(seed + 17L) < -0.7) continue;
            int nodes = 4 + (int)Math.round(Math.abs(Render3DUtil.lightningNoise(seed + 29L)) * 2.0);
            double angle = Math.PI * 2 * (double)bolt / 8.0 + Render3DUtil.lightningNoise(seed + 41L) * 0.48;
            class_243 radial = new class_243(Math.cos(angle), 0.0, Math.sin(angle));
            double startRadius = radius * (0.55 + Math.abs(Render3DUtil.lightningNoise(seed + 53L)) * 0.45);
            double startY = height * (0.12 + Math.abs(Render3DUtil.lightningNoise(seed + 67L)) * 0.72);
            class_243 origin = target.method_1031(radial.field_1352 * startRadius, startY, radial.field_1350 * startRadius);
            double vertical = Render3DUtil.lightningNoise(seed + 79L) * 0.72;
            class_243 direction = new class_243(-Math.sin(angle), vertical, Math.cos(angle)).method_1029();
            double length = radius * (0.85 + Math.abs(Render3DUtil.lightningNoise(seed + 83L)) * 0.85);
            class_243 previous = origin;
            for (int node = 1; node <= nodes; ++node) {
                double progress = (double)node / (double)nodes;
                class_243 base = origin.method_1019(direction.method_1021(length * progress));
                double sideJitter = Render3DUtil.lightningNoise(seed + (long)node * 97L) * radius * 0.28;
                double yJitter = Render3DUtil.lightningNoise(seed + (long)node * 113L) * height * 0.055;
                class_243 current = base.method_1031(radial.field_1352 * sideJitter, yJitter, radial.field_1350 * sideJitter);
                Render3DUtil.drawLightningGlow(previous, current, wideGlowColor, 0.115f, 0.0f, canSee);
                Render3DUtil.drawLightningGlow(previous, current, glowColor, 0.032f, 0.3f, canSee);
                Render3DUtil.drawLineGradient(previous, current, coreColor, coreColor, 1.35f, canSee);
                if (node == 2 && Render3DUtil.lightningNoise(seed + 127L) > 0.15) {
                    double forkLength = radius * (0.3 + Math.abs(Render3DUtil.lightningNoise(seed + 139L)) * 0.3);
                    class_243 forkDirection = radial.method_1021(0.72).method_1019(direction.method_1021(0.3)).method_1031(0.0, Render3DUtil.lightningNoise(seed + 151L) * 0.45, 0.0).method_1029();
                    class_243 branch = current.method_1019(forkDirection.method_1021(forkLength));
                    int branchGlow = ColorUtil.multAlpha(glowColor, 0.72f);
                    int branchCore = ColorUtil.multAlpha(coreColor, 0.86f);
                    Render3DUtil.drawLightningGlow(current, branch, branchGlow, 0.07f, 0.0f, canSee);
                    Render3DUtil.drawLineGradient(current, branch, branchCore, ColorUtil.multAlpha(branchCore, 0.0f), 0.95f, canSee);
                }
                previous = current;
            }
        }
    }

    public static void drawLightningAt(class_243 pos, float progress, float fade, long seedBase) {
        int theme;
        if (Render3DUtil.mc.field_1724 == null || fade <= 0.001f) {
            return;
        }
        int boltColor = theme = ColorUtil.getClientColor();
        int glowColor = ColorUtil.multAlpha(ColorUtil.multBright(boltColor, 0.67f), 0.5f * fade);
        int wideGlowColor = ColorUtil.multAlpha(ColorUtil.multBright(boltColor, 0.58f), 0.4f * fade);
        int coreColor = ColorUtil.multAlpha(ColorUtil.overCol(boltColor, ColorUtil.getColor(255, 255, 255, 255), 0.78f), 0.98f * fade);
        double radius = 0.5 + (double)progress * 1.6;
        double height = 1.8;
        boolean canSee = true;
        for (int bolt = 0; bolt < 16; ++bolt) {
            long seed = seedBase + (long)bolt * 132897987541L;
            if (Render3DUtil.lightningNoise(seed + 17L) < -0.6) continue;
            int nodes = 4 + (int)Math.round(Math.abs(Render3DUtil.lightningNoise(seed + 29L)) * 2.0);
            double angle = Math.PI * 2 * (double)bolt / 16.0 + Render3DUtil.lightningNoise(seed + 41L) * 0.48;
            class_243 radial = new class_243(Math.cos(angle), 0.0, Math.sin(angle));
            double startRadius = radius * (0.3 + Math.abs(Render3DUtil.lightningNoise(seed + 53L)) * 0.3);
            double startY = height * (0.12 + Math.abs(Render3DUtil.lightningNoise(seed + 67L)) * 0.72);
            class_243 origin = pos.method_1031(radial.field_1352 * startRadius, startY, radial.field_1350 * startRadius);
            double vertical = Render3DUtil.lightningNoise(seed + 79L) * 0.85;
            class_243 direction = new class_243(radial.field_1352, vertical, radial.field_1350).method_1029();
            double length = radius * (0.8 + Math.abs(Render3DUtil.lightningNoise(seed + 83L)) * 0.8);
            class_243 previous = origin;
            for (int node = 1; node <= nodes; ++node) {
                double nodeProgress = (double)node / (double)nodes;
                class_243 base = origin.method_1019(direction.method_1021(length * nodeProgress));
                double sideJitter = Render3DUtil.lightningNoise(seed + (long)node * 97L) * radius * 0.32;
                double yJitter = Render3DUtil.lightningNoise(seed + (long)node * 113L) * height * 0.065;
                class_243 current = base.method_1031(radial.field_1352 * sideJitter, yJitter, radial.field_1350 * sideJitter);
                Render3DUtil.drawLightningGlow(previous, current, wideGlowColor, 0.12f, 0.0f, canSee);
                Render3DUtil.drawLightningGlow(previous, current, glowColor, 0.036f, 0.3f, canSee);
                Render3DUtil.drawLineGradient(previous, current, coreColor, coreColor, 1.4f, canSee);
                if (node == 2 && Render3DUtil.lightningNoise(seed + 127L) > 0.1) {
                    double forkLength = radius * (0.35 + Math.abs(Render3DUtil.lightningNoise(seed + 139L)) * 0.35);
                    class_243 forkDirection = radial.method_1021(0.72).method_1019(direction.method_1021(0.3)).method_1031(0.0, Render3DUtil.lightningNoise(seed + 151L) * 0.5, 0.0).method_1029();
                    class_243 branch = current.method_1019(forkDirection.method_1021(forkLength));
                    int branchGlow = ColorUtil.multAlpha(glowColor, 0.72f);
                    int branchCore = ColorUtil.multAlpha(coreColor, 0.86f);
                    Render3DUtil.drawLightningGlow(current, branch, branchGlow, 0.075f, 0.0f, canSee);
                    Render3DUtil.drawLineGradient(current, branch, branchCore, ColorUtil.multAlpha(branchCore, 0.0f), 1.0f, canSee);
                }
                previous = current;
            }
        }
    }

    private static double lightningNoise(long value) {
        value = (value ^ value >>> 33) * -49064778989728563L;
        value = (value ^ value >>> 33) * -4265267296055464877L;
        value ^= value >>> 33;
        return (double)(value >>> 11) * (double)1.110223E-16f * 2.0 - 1.0;
    }

    private static void drawLightningGlow(class_243 start, class_243 end, int color, float width, float coreStrength, boolean depth) {
        LightningSegment segment = new LightningSegment(start, end, color, width, coreStrength);
        if (depth) {
            LIGHTNING_DEPTH.add(segment);
        } else {
            LIGHTNING.add(segment);
        }
    }

    public static void drawBlackFlash(class_4587 matrices, class_1309 targetEntity, float anim, float red) {
        if (Render3DUtil.mc.field_1724 == null || targetEntity == null) {
            return;
        }
        class_4184 camera = Render3DUtil.mc.field_1773.method_19418();
        class_243 targetBase = MathUtil.interpolate((class_1297)targetEntity);
        class_243 cameraPos = camera.method_71156();
        class_243 targetVec = targetBase.method_1020(cameraPos);
        float fade = Render3DUtil.smoothTargetFade(anim);
        if (fade <= 0.001f) {
            return;
        }
        float hurt = class_3532.method_15363((float)red, (float)0.0f, (float)1.0f);
        double entityHeight = Math.max(0.9, (double)targetEntity.method_17682());
        double centerY = entityHeight * 0.52;
        long now = System.nanoTime();
        double dt = blackFlashLastTime == 0L ? 0.016 : (double)(now - blackFlashLastTime) * 1.0E-9;
        blackFlashLastTime = now;
        if (dt <= 0.0 || dt > 0.1) {
            dt = 0.016;
        }
        double animSpeed = 15.0;
        double t = blackFlashPhase += dt * animSpeed;
        long targetSeed = (long)targetEntity.method_5628() * 492871329L;
        ArrayList<FlashBolt> boltPaths = new ArrayList<FlashBolt>();
        Render3DUtil.generateBlackFlashFractal(targetSeed, entityHeight, hurt, t, boltPaths);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.depthMask(false);
        matrices.method_22903();
        matrices.method_22904(targetVec.field_1352, targetVec.field_1351 + centerY, targetVec.field_1350);
        matrices.method_22907((Quaternionfc)camera.method_23767());
        Matrix4f matrix = matrices.method_23760().method_23761();
        float pulse = (float)(0.94 + 0.06 * Math.sin(t * 1.3));
        float baseStrokeWidth = 0.038f * pulse;
        LegacyRenderSystem.setShaderTexture(0, bloom);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        class_287 bloomBuf = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        float bloomPulse = (float)(0.92 + 0.08 * Math.sin(t * 1.2));
        float centerBloomSize = (0.75f + hurt * 0.3f) * bloomPulse;
        int centerBloomColor = ColorUtil.getColor(255, 18, 32, (int)((65.0f + hurt * 30.0f) * fade));
        Render3DUtil.emitBloomParticle(bloomBuf, matrix, 0.0, 0.0, 0.0, centerBloomSize, centerBloomColor);
        int wideGlowColor = ColorUtil.getColor(255, 15, 28, (int)(38.0f * fade));
        int tightGlowColor = ColorUtil.getColor(255, 40, 60, (int)(85.0f * fade));
        for (FlashBolt bolt : boltPaths) {
            float bScale = bolt.scale();
            Render3DUtil.renderPathPngGlow(bloomBuf, matrix, bolt.path(), 0.28f * bScale, wideGlowColor, 0.038);
            Render3DUtil.renderPathPngGlow(bloomBuf, matrix, bolt.path(), 0.13f * bScale, tightGlowColor, 0.026);
        }
        BufferRenderer.drawWithGlobalProgram(bloomBuf.method_60800());
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        class_287 blackCoreBuffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        int blackCoreColor = ColorUtil.getColor(0, 0, 0, (int)(255.0f * fade));
        float coreWidth = baseStrokeWidth * 0.85f;
        for (FlashBolt bolt : boltPaths) {
            Render3DUtil.renderSolidRibbon(blackCoreBuffer, matrix, bolt.path(), coreWidth * bolt.scale(), blackCoreColor);
        }
        BufferRenderer.drawWithGlobalProgram(blackCoreBuffer.method_60800());
        matrices.method_22909();
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
    }

    public static void drawBlackFlashAt(class_4587 matrices, class_243 worldPos, float progress, float fade, long seed) {
        if (Render3DUtil.mc.field_1724 == null || worldPos == null || fade <= 0.001f) {
            return;
        }
        class_4184 camera = Render3DUtil.mc.field_1773.method_19418();
        class_243 cameraPos = camera.method_71156();
        class_243 targetVec = worldPos.method_1020(cameraPos);
        double entityHeight = 1.8;
        double centerY = entityHeight * 0.5;
        double t = (double)progress * 28.0 + (double)(seed % 1000L) * 0.01;
        ArrayList<FlashBolt> boltPaths = new ArrayList<FlashBolt>();
        Render3DUtil.generateDenseKillFlashFractal(seed, entityHeight, progress, t, boltPaths);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.depthMask(false);
        matrices.method_22903();
        matrices.method_22904(targetVec.field_1352, targetVec.field_1351 + centerY, targetVec.field_1350);
        matrices.method_22907((Quaternionfc)camera.method_23767());
        Matrix4f matrix = matrices.method_23760().method_23761();
        float pulse = (float)(0.94 + 0.06 * Math.sin(t * 1.3));
        float baseStrokeWidth = 0.045f * pulse;
        LegacyRenderSystem.setShaderTexture(0, bloom);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        class_287 bloomBuf = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        float centerBloomSize = 1.3f - progress * 0.4f;
        int centerBloomColor = ColorUtil.getColor(255, 18, 32, (int)(110.0f * fade));
        Render3DUtil.emitBloomParticle(bloomBuf, matrix, 0.0, 0.0, 0.0, centerBloomSize, centerBloomColor);
        int wideGlowColor = ColorUtil.getColor(255, 15, 28, (int)(48.0f * fade));
        int tightGlowColor = ColorUtil.getColor(255, 40, 60, (int)(110.0f * fade));
        for (FlashBolt bolt : boltPaths) {
            float bScale = bolt.scale();
            Render3DUtil.renderPathPngGlow(bloomBuf, matrix, bolt.path(), 0.34f * bScale, wideGlowColor, 0.038);
            Render3DUtil.renderPathPngGlow(bloomBuf, matrix, bolt.path(), 0.16f * bScale, tightGlowColor, 0.026);
        }
        BufferRenderer.drawWithGlobalProgram(bloomBuf.method_60800());
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        class_287 blackCoreBuffer = ((class_289)tessellator.get()).method_60827(VertexFormat.class_5596.field_27382, class_290.field_1576);
        int blackCoreColor = ColorUtil.getColor(0, 0, 0, (int)(255.0f * fade));
        float coreWidth = baseStrokeWidth * 0.9f;
        for (FlashBolt bolt : boltPaths) {
            Render3DUtil.renderSolidRibbon(blackCoreBuffer, matrix, bolt.path(), coreWidth * bolt.scale(), blackCoreColor);
        }
        BufferRenderer.drawWithGlobalProgram(blackCoreBuffer.method_60800());
        matrices.method_22909();
        LegacyRenderSystem.depthMask(true);
        LegacyRenderSystem.enableDepthTest();
        LegacyRenderSystem.enableCull();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableBlend();
    }

    private static void generateDenseKillFlashFractal(long seed, double h, float progress, double t, List<FlashBolt> out) {
        float slashLife = Math.max(0.0f, 1.0f - progress / 0.3f);
        if (slashLife > 0.01f) {
            double[][] slashes = new double[][]{{0.0, 11.0}, {1.05, 37.0}, {2.1, 79.0}};
            for (int s = 0; s < slashes.length; ++s) {
                double ang = slashes[s][0] + t * 0.35;
                double len = h * (0.65 + 0.15 * Math.sin(t * 0.8 + (double)s)) * (double)slashLife;
                double cos = Math.cos(ang) * len;
                double sin = Math.sin(ang) * len;
                class_243 start = new class_243(-cos, -sin, 0.0);
                class_243 end = new class_243(cos, sin, 0.0);
                ArrayList<class_243> bolt = new ArrayList<class_243>();
                bolt.add(start);
                Render3DUtil.buildFractalSegments(start, end, 0.28 * (double)slashLife, seed + (long)slashes[s][1], 3, t, bolt);
                out.add(new FlashBolt(bolt, slashLife));
                int pts = bolt.size();
                if (pts < 5) continue;
                class_243 elbow = (class_243)bolt.get(pts / 2);
                class_243 d = ((class_243)bolt.get(pts / 2 + 1)).method_1020((class_243)bolt.get(pts / 2 - 1)).method_1029();
                class_243 perp = new class_243(-d.field_1351, d.field_1352, 0.0);
                double bAngle = Math.atan2(perp.field_1351, perp.field_1352) + Math.sin(t * 1.2 + (double)s) * 0.5;
                double bLen = 0.42 * (double)slashLife;
                class_243 bEnd = elbow.method_1031(Math.cos(bAngle) * bLen, Math.sin(bAngle) * bLen, 0.0);
                ArrayList<class_243> branch = new ArrayList<class_243>();
                branch.add(elbow);
                Render3DUtil.buildFractalSegments(elbow, bEnd, 0.18 * (double)slashLife, seed + 103L + (long)s * 17L, 2, t, branch);
                out.add(new FlashBolt(branch, 0.85f * slashLife));
            }
        }
        float outwardEase = (float)(1.0 - Math.pow(1.0 - (double)progress, 1.8));
        float sparkLife = (float)Math.pow(Math.max(0.0f, 1.0f - progress), 1.4);
        int sparkCount = 28;
        for (int i = 0; i < sparkCount; ++i) {
            long sparkSeed = seed ^ (long)(i + 1) * 5871781006564002453L;
            double angleOffset = (Render3DUtil.pseudoRandom(sparkSeed) - 0.5) * 0.35;
            double baseAngle = (double)i * (Math.PI * 2 / (double)sparkCount) + angleOffset;
            double angle = baseAngle + Math.sin(t * 0.8 + (double)i) * 0.12;
            double maxSpread = 1.6 + Render3DUtil.pseudoRandom(sparkSeed + 31L) * 2.2;
            double currentDist = (double)outwardEase * maxSpread;
            double x = Math.cos(angle) * currentDist;
            double y = Math.sin(angle) * currentDist * 0.85 + (Render3DUtil.pseudoRandom(sparkSeed + 89L) - 0.5) * 0.3;
            class_243 head = new class_243(x, y, 0.0);
            double sparkLen = (0.24 + Render3DUtil.pseudoRandom(sparkSeed + 101L) * 0.28) * (1.0 - (double)progress * 0.6);
            class_243 dir = new class_243(Math.cos(angle), Math.sin(angle) * 0.85, 0.0).method_1029();
            class_243 tail = head.method_1020(dir.method_1021(sparkLen));
            ArrayList<class_243> sparkBolt = new ArrayList<class_243>();
            sparkBolt.add(tail);
            Render3DUtil.buildFractalSegments(tail, head, 0.06 * (1.0 - (double)progress * 0.5), sparkSeed + 99L, 2, t, sparkBolt);
            float scale = (0.65f + (float)Render3DUtil.pseudoRandom(sparkSeed + 55L) * 0.45f) * sparkLife;
            out.add(new FlashBolt(sparkBolt, scale));
        }
        int burstCount = 10;
        for (int b = 0; b < burstCount; ++b) {
            long burstSeed = seed ^ (long)(b + 1) * 7127433736153653287L;
            double bAngle = (double)b * (Math.PI * 2 / (double)burstCount) + (Render3DUtil.pseudoRandom(burstSeed) - 0.5) * 0.4;
            double bMaxDist = 1.4 + Render3DUtil.pseudoRandom(burstSeed + 17L) * 1.8;
            double bDist = (double)outwardEase * bMaxDist;
            class_243 bStart = new class_243(Math.cos(bAngle) * (bDist * 0.35), Math.sin(bAngle) * (bDist * 0.35), 0.0);
            class_243 bEnd = new class_243(Math.cos(bAngle) * bDist, Math.sin(bAngle) * bDist, 0.0);
            ArrayList<class_243> burstBolt = new ArrayList<class_243>();
            burstBolt.add(bStart);
            Render3DUtil.buildFractalSegments(bStart, bEnd, 0.1 * (1.0 - (double)progress * 0.5), burstSeed + 43L, 2, t, burstBolt);
            out.add(new FlashBolt(burstBolt, 0.7f * sparkLife));
        }
    }

    private static void generateBlackFlashFractal(long seed, double h, float hurt, double t, List<FlashBolt> out) {
        double tMain = t * 0.65;
        double sweepX1 = Math.sin(tMain * 1.1) * 0.35 + Math.cos(tMain * 0.47) * 0.12;
        double sweepY1 = h * 0.42 + Math.cos(tMain * 0.73) * (h * 0.1);
        double sweepX2 = -Math.sin(tMain * 0.93 + 1.2) * 0.35 - Math.cos(tMain * 0.55) * 0.12;
        double sweepY2 = -h * 0.42 + Math.sin(tMain * 0.61) * (h * 0.1);
        class_243 slashStart = new class_243(sweepX1, sweepY1, 0.0);
        class_243 slashEnd = new class_243(sweepX2, sweepY2, 0.0);
        ArrayList<class_243> mainBolt = new ArrayList<class_243>();
        mainBolt.add(slashStart);
        Render3DUtil.buildFractalSegments(slashStart, slashEnd, 0.26, seed + 11L, 3, t, mainBolt);
        out.add(new FlashBolt(mainBolt, 1.0f));
        int mainPts = mainBolt.size();
        if (mainPts >= 5) {
            class_243 elbow1 = (class_243)mainBolt.get(2);
            class_243 d1 = ((class_243)mainBolt.get(3)).method_1020((class_243)mainBolt.get(1)).method_1029();
            class_243 perp1 = new class_243(-d1.field_1351, d1.field_1352, 0.0);
            double branchAngle1 = Math.atan2(perp1.field_1351, perp1.field_1352) + Math.sin(t * 1.1) * 0.4;
            double branchLen1 = 0.36 + Math.sin(t * 0.8) * 0.08;
            class_243 branchEnd1 = elbow1.method_1031(Math.cos(branchAngle1) * branchLen1, Math.sin(branchAngle1) * branchLen1, 0.0);
            ArrayList<class_243> branch1 = new ArrayList<class_243>();
            branch1.add(elbow1);
            Render3DUtil.buildFractalSegments(elbow1, branchEnd1, 0.16, seed + 31L, 2, t, branch1);
            out.add(new FlashBolt(branch1, 0.8f));
            class_243 elbow2 = (class_243)mainBolt.get(Math.min(mainPts - 3, 5));
            class_243 d2 = ((class_243)mainBolt.get(Math.min(mainPts - 2, 6))).method_1020((class_243)mainBolt.get(Math.min(mainPts - 4, 4))).method_1029();
            class_243 perp2 = new class_243(d2.field_1351, -d2.field_1352, 0.0);
            double branchAngle2 = Math.atan2(perp2.field_1351, perp2.field_1352) + Math.cos(t * 1.2) * 0.4;
            double branchLen2 = 0.34 + Math.cos(t * 0.7) * 0.08;
            class_243 branchEnd2 = elbow2.method_1031(Math.cos(branchAngle2) * branchLen2, Math.sin(branchAngle2) * branchLen2, 0.0);
            ArrayList<class_243> branch2 = new ArrayList<class_243>();
            branch2.add(elbow2);
            Render3DUtil.buildFractalSegments(elbow2, branchEnd2, 0.15, seed + 77L, 2, t, branch2);
            out.add(new FlashBolt(branch2, 0.75f));
        }
        double crossX1 = -Math.cos(tMain * 0.75) * 0.4;
        double crossY1 = Math.sin(tMain * 0.85) * 0.16 + h * 0.05;
        double crossX2 = Math.cos(tMain * 0.75) * 0.4;
        double crossY2 = -Math.sin(tMain * 0.85) * 0.16 - h * 0.05;
        class_243 chestLeft = new class_243(crossX1, crossY1, 0.0);
        class_243 chestRight = new class_243(crossX2, crossY2, 0.0);
        ArrayList<class_243> chestBolt = new ArrayList<class_243>();
        chestBolt.add(chestLeft);
        Render3DUtil.buildFractalSegments(chestLeft, chestRight, 0.18, seed + 51L, 2, t, chestBolt);
        out.add(new FlashBolt(chestBolt, 0.82f));
        int sparkCount = 7 + (int)(hurt * 4.0f);
        for (int i = 0; i < sparkCount; ++i) {
            float intensity;
            double phase = t * 1.2 + (double)i * 0.85;
            long cycle = (long)Math.floor(phase / Math.PI);
            double u = phase % Math.PI / Math.PI;
            if (u < 0.0) {
                u += 1.0;
            }
            if (!((intensity = (float)Math.sin(u * Math.PI)) > 0.12f)) continue;
            long sparkSeed = seed ^ cycle * -7046029254386353131L ^ (long)(i + 1) * 5871781006564002453L;
            double rAngle = Render3DUtil.pseudoRandom(sparkSeed);
            double rLen = Render3DUtil.pseudoRandom(sparkSeed + 101L);
            double rY = Render3DUtil.pseudoRandom(sparkSeed + 211L);
            double rX = Render3DUtil.pseudoRandom(sparkSeed + 307L);
            double baseAngle = (double)i * 0.85 + rAngle * (Math.PI * 2);
            double angle = baseAngle + Math.sin(t * 0.9 + (double)i) * 0.2;
            double sparkLen = (0.22 + rLen * 0.28) * (1.0 + (double)hurt * 0.3) * (double)intensity;
            double origY = (rY * 2.0 - 1.0) * h * 0.38 + Math.sin(t * 0.8) * 0.08;
            double origX = (rX * 2.0 - 1.0) * 0.24 + Math.cos(t * 0.7) * 0.12;
            class_243 sparkStart = new class_243(origX, origY, 0.0);
            class_243 sparkEnd = sparkStart.method_1031(Math.cos(angle) * sparkLen, Math.sin(angle) * sparkLen * 0.85, 0.0);
            ArrayList<class_243> sparkBolt = new ArrayList<class_243>();
            sparkBolt.add(sparkStart);
            int depth = sparkLen > 0.28 ? 2 : 1;
            Render3DUtil.buildFractalSegments(sparkStart, sparkEnd, 0.07 * (double)intensity, sparkSeed + 99L, depth, t, sparkBolt);
            out.add(new FlashBolt(sparkBolt, 0.6f * intensity));
        }
        if (hurt > 0.05f) {
            class_243 center = new class_243(0.0, h * 0.05, 0.0);
            double burstDist = 0.55 + (double)hurt * 0.5;
            long burstCycle = (long)Math.floor(t * 0.75);
            int burstCount = 5;
            for (int b = 0; b < burstCount; ++b) {
                long burstSeed = seed ^ burstCycle * 7809847782465536322L ^ (long)b * 7127433736153653287L;
                double bAngle = Render3DUtil.pseudoRandom(burstSeed) * Math.PI * 2.0 + Math.sin(t * 0.9 + (double)b) * 0.2;
                double bDist = burstDist * (0.7 + Render3DUtil.pseudoRandom(burstSeed + 17L) * 0.55);
                class_243 rayEnd = center.method_1031(Math.cos(bAngle) * bDist, Math.sin(bAngle) * bDist * 0.85, 0.0);
                ArrayList<class_243> ray = new ArrayList<class_243>();
                ray.add(center);
                Render3DUtil.buildFractalSegments(center, rayEnd, 0.14, burstSeed, 2, t, ray);
                out.add(new FlashBolt(ray, 0.7f));
            }
        }
    }

    private static void buildFractalSegments(class_243 start, class_243 end, double displacement, long seed, int depth, double t, List<class_243> points) {
        if (depth <= 0) {
            points.add(end);
            return;
        }
        class_243 dir = end.method_1020(start);
        double len = dir.method_1033();
        if (len < 1.0E-4) {
            points.add(end);
            return;
        }
        class_243 perp = new class_243(-dir.field_1351, dir.field_1352, 0.0);
        perp = perp.method_1027() < 1.0E-6 ? new class_243(1.0, 0.0, 0.0) : perp.method_1029();
        class_243 along = dir.method_1021(1.0 / len);
        double sVal = (double)(seed % 1009L) * 0.043;
        double w1 = Math.sin(t * 1.25 + sVal);
        double w2 = Math.cos(t * 2.15 + sVal * 1.618 + (double)depth * 0.85);
        double w3 = Math.sin(t * 0.65 + sVal * 2.718);
        double bendFactor = w1 * 0.75 + w2 * 0.5 + w3 * 0.35;
        double midFrac = 0.5 + Math.sin(t * 1.1 + sVal * 0.5) * 0.14;
        class_243 baseMid = start.method_1019(dir.method_1021(midFrac));
        double perpAmount = displacement * bendFactor;
        double alongAmount = displacement * Math.cos(t * 1.5 + sVal) * 0.22;
        class_243 displacedMid = baseMid.method_1019(perp.method_1021(perpAmount)).method_1019(along.method_1021(alongAmount));
        Render3DUtil.buildFractalSegments(start, displacedMid, displacement * 0.58, seed * 31L + 101L, depth - 1, t, points);
        Render3DUtil.buildFractalSegments(displacedMid, end, displacement * 0.58, seed * 37L + 211L, depth - 1, t, points);
    }

    private static void renderPathPngGlow(class_287 builder, Matrix4f matrix, List<class_243> path, float glowSize, int color, double step) {
        int count = path.size();
        if (count < 2) {
            return;
        }
        for (int i = 0; i < count - 1; ++i) {
            int s;
            class_243 p1 = path.get(i);
            class_243 p2 = path.get(i + 1);
            double dist = p1.method_1022(p2);
            int steps = Math.max(1, (int)Math.ceil(dist / step));
            int n = s = i == 0 ? 0 : 1;
            while (s <= steps) {
                double frac = (double)s / (double)steps;
                class_243 pt = p1.method_35590(p2, frac);
                double pathProgress = ((double)i + frac) / (double)(count - 1);
                float taper = (float)Math.sin(pathProgress * Math.PI);
                float sSize = glowSize * (0.65f + taper * 0.35f);
                Render3DUtil.emitBloomParticle(builder, matrix, pt.field_1352, pt.field_1351, pt.field_1350, sSize, color);
                ++s;
            }
        }
    }

    private static void emitBloomParticle(class_287 builder, Matrix4f matrix, double px, double py, double pz, float size, int color) {
        float half = size * 0.5f;
        float x1 = (float)(px - (double)half);
        float y1 = (float)(py - (double)half);
        float x2 = (float)(px + (double)half);
        float y2 = (float)(py + (double)half);
        float z = (float)pz;
        builder.method_22918((Matrix4fc)matrix, x1, y2, z).method_22913(0.0f, 1.0f).method_39415(color);
        builder.method_22918((Matrix4fc)matrix, x2, y2, z).method_22913(1.0f, 1.0f).method_39415(color);
        builder.method_22918((Matrix4fc)matrix, x2, y1, z).method_22913(1.0f, 0.0f).method_39415(color);
        builder.method_22918((Matrix4fc)matrix, x1, y1, z).method_22913(0.0f, 0.0f).method_39415(color);
    }

    private static void renderSolidRibbon(class_287 buffer, Matrix4f matrix, List<class_243> path, float maxStrokeWidth, int color) {
        int i;
        int count = path.size();
        if (count < 2) {
            return;
        }
        class_243[] normals = new class_243[count];
        float[] widths = new float[count];
        for (i = 0; i < count; ++i) {
            class_243 d2;
            class_243 d;
            float t = (float)Math.sin((double)i / (double)(count - 1) * Math.PI);
            widths[i] = maxStrokeWidth * (0.35f + t * 0.65f);
            if (count == 2) {
                d = path.get(1).method_1020(path.get(0)).method_1029();
                normals[i] = new class_243(-d.field_1351, d.field_1352, 0.0);
                continue;
            }
            if (i == 0) {
                d = path.get(1).method_1020(path.get(0)).method_1029();
                normals[i] = new class_243(-d.field_1351, d.field_1352, 0.0);
                continue;
            }
            if (i == count - 1) {
                d = path.get(count - 1).method_1020(path.get(count - 2)).method_1029();
                normals[i] = new class_243(-d.field_1351, d.field_1352, 0.0);
                continue;
            }
            class_243 d1 = path.get(i).method_1020(path.get(i - 1)).method_1029();
            class_243 tangent = d1.method_1019(d2 = path.get(i + 1).method_1020(path.get(i)).method_1029());
            if (tangent.method_1027() < 1.0E-6) {
                normals[i] = new class_243(-d1.field_1351, d1.field_1352, 0.0);
                continue;
            }
            tangent = tangent.method_1029();
            double dot = d1.field_1352 * tangent.field_1352 + d1.field_1351 * tangent.field_1351;
            double miter = Math.max(0.6, Math.min(1.6, 1.0 / Math.max(0.25, dot)));
            normals[i] = new class_243(-tangent.field_1351 * miter, tangent.field_1352 * miter, 0.0);
        }
        for (i = 0; i < count - 1; ++i) {
            class_243 p1 = path.get(i);
            class_243 p2 = path.get(i + 1);
            class_243 n1 = normals[i];
            class_243 n2 = normals[i + 1];
            float w1 = widths[i] * 0.5f;
            float w2 = widths[i + 1] * 0.5f;
            float p1x = (float)p1.field_1352;
            float p1y = (float)p1.field_1351;
            float p1z = (float)p1.field_1350;
            float p2x = (float)p2.field_1352;
            float p2y = (float)p2.field_1351;
            float p2z = (float)p2.field_1350;
            float l1x = (float)(p1.field_1352 - n1.field_1352 * (double)w1);
            float l1y = (float)(p1.field_1351 - n1.field_1351 * (double)w1);
            float r1x = (float)(p1.field_1352 + n1.field_1352 * (double)w1);
            float r1y = (float)(p1.field_1351 + n1.field_1351 * (double)w1);
            float l2x = (float)(p2.field_1352 - n2.field_1352 * (double)w2);
            float l2y = (float)(p2.field_1351 - n2.field_1351 * (double)w2);
            float r2x = (float)(p2.field_1352 + n2.field_1352 * (double)w2);
            float r2y = (float)(p2.field_1351 + n2.field_1351 * (double)w2);
            buffer.method_22918((Matrix4fc)matrix, l1x, l1y, p1z).method_39415(color);
            buffer.method_22918((Matrix4fc)matrix, r1x, r1y, p1z).method_39415(color);
            buffer.method_22918((Matrix4fc)matrix, r2x, r2y, p2z).method_39415(color);
            buffer.method_22918((Matrix4fc)matrix, l2x, l2y, p2z).method_39415(color);
        }
        Render3DUtil.emitJointDisc(buffer, matrix, path.get(0), widths[0] * 0.5f, color);
        Render3DUtil.emitJointDisc(buffer, matrix, path.get(count - 1), widths[count - 1] * 0.5f, color);
    }

    private static void emitJointDisc(class_287 buffer, Matrix4f matrix, class_243 p, float radius, int color) {
        float px = (float)p.field_1352;
        float py = (float)p.field_1351;
        float pz = (float)p.field_1350;
        float r = radius * 0.5f;
        float d = r * 0.7071f;
        buffer.method_22918((Matrix4fc)matrix, px - r, py, pz).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, px, py - r, pz).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, px + r, py, pz).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, px, py + r, pz).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, px - d, py - d, pz).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, px + d, py - d, pz).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, px + d, py + d, pz).method_39415(color);
        buffer.method_22918((Matrix4fc)matrix, px - d, py + d, pz).method_39415(color);
    }

    private static double smoothCircleSin(double input) {
        double sine = (Math.sin(input) + 1.0) / 2.0;
        double original = -(Math.cos(Math.PI * sine) - 1.0) / 2.0;
        return Render3DUtil.lerp(sine, original, 0.58);
    }

    private static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    public static void drawTexture(class_4587.class_4665 entry, class_2960 id, float x, float y, float width, float height, Vector4i color, boolean depth) {
        Texture texture = new Texture(entry, id, x, y, width, height, color, Render3DUtil.needsAdditiveBlend(id));
        if (depth) {
            TEXTURE_DEPTH.add(texture);
        } else {
            TEXTURE.add(texture);
        }
    }

    /** A flat panel showing the whole texture the right way up: (x, y) is the bottom-left corner, y goes up. */
    public static void drawPage(class_4587.class_4665 entry, class_2960 id, float x, float y, float width, float height, int color) {
        PAGE.add(new Texture(entry, id, x, y, width, height, new Vector4i(color), false));
    }

    public static void drawTextureAlpha(class_4587.class_4665 entry, class_2960 id, float x, float y, float width, float height, Vector4i color, boolean depth) {
        Texture texture = new Texture(entry, id, x, y, width, height, color, false);
        if (depth) {
            TEXTURE_DEPTH.add(texture);
        } else {
            TEXTURE.add(texture);
        }
    }

    public static void drawTextureAdditive(class_4587.class_4665 entry, class_2960 id, float x, float y, float width, float height, Vector4i color, boolean depth) {
        Texture texture = new Texture(entry, id, x, y, width, height, color, true);
        if (depth) {
            TEXTURE_DEPTH.add(texture);
        } else {
            TEXTURE.add(texture);
        }
    }

    private static boolean needsAdditiveBlend(class_2960 id) {
        String path = id.method_12832();
        return path.equals("textures/capture.png") || path.equals("textures/bloom.png") || path.equals("textures/circle.png") || path.equals("textures/circle_bold.png") || path.equals("textures/particles/firefly.png") || path.contains("/bloom/") || path.contains("firefly");
    }

    private static double pseudoRandom(long s) {
        s = (s ^ s >>> 30) * -4658895280553007687L;
        s = (s ^ s >>> 27) * -7723592293110705685L;
        s ^= s >>> 31;
        return (double)(s & Integer.MAX_VALUE) / 2.147483647E9;
    }

    private Render3DUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    static {
        for (int segment = 0; segment < 47; ++segment) {
            double strength = 1.0 - (double)segment / 46.0;
            Render3DUtil.GHOST_STRENGTH_SIZE[segment] = Math.pow(strength, 0.6);
            Render3DUtil.GHOST_STRENGTH_WHITE[segment] = Math.pow(strength, 0.9);
            Render3DUtil.GHOST_STRENGTH_COLOR[segment] = Math.pow(strength, 1.1);
        }
        CRYSTAL_VERTICES = new Vector3f[]{new Vector3f(0.0f, 1.5f, 0.0f), new Vector3f(0.0f, -1.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f)};
        CRYSTAL_FACES = new int[][]{{0, 4, 2}, {0, 3, 4}, {0, 5, 3}, {0, 2, 5}, {1, 2, 4}, {1, 4, 3}, {1, 3, 5}, {1, 5, 2}};
        CRYSTAL_BRIGHTNESS = new float[]{1.0f, 0.8f, 0.6f, 0.9f, 0.7f, 0.5f, 0.4f, 0.6f};
        espValue = 1.0f;
        espSpeed = 1.0f;
        targetEspUpdateAge = Integer.MIN_VALUE;
        crystalCircleY = Double.NaN;
    }

    public record Texture(class_4587.class_4665 entry, class_2960 id, float x, float y, float width, float height, Vector4i color, boolean additiveBlend) {
    }

    public record TextureBatch(class_2960 id, boolean additiveBlend) {
    }

    public record Line(class_4587.class_4665 entry, class_243 start, class_243 end, int colorStart, int colorEnd, float width) {
    }

    public record LightningSegment(class_243 start, class_243 end, int color, float width, float coreStrength) {
    }

    private record LightningBatchKey(int color, float coreStrength) {
    }

    public record GradientQuad(class_4587.class_4665 entry, class_243 x, class_243 y, class_243 w, class_243 z, int colorX, int colorY, int colorW, int colorZ) {
    }

    public record Quad(class_4587.class_4665 entry, class_243 x, class_243 y, class_243 w, class_243 z, int color) {
    }

    private record FlashBolt(List<class_243> path, float scale) {
    }
}

