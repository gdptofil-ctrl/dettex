/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.SourceFactor
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10799
 *  net.minecraft.class_12247
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_1921
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_3532
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.recoin.implement.features.modules.render.esp;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1921;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;

public class LightningTargetESP
implements QuickImports {
    private static final class_1921 VISIBLE_LINES = LightningTargetESP.createLayer("target_lightning", DepthTestFunction.LEQUAL_DEPTH_TEST);
    private static final class_1921 OCCLUDED_LINES = LightningTargetESP.createLayer("target_lightning_xray", DepthTestFunction.GREATER_DEPTH_TEST);
    private float lightningTime = 0.0f;
    private long lastLightningNano = 0L;
    private float occlusion;
    private float occlusionFrom;
    private float occlusionTo;
    private float occlusionElapsed;

    private static class_1921 createLayer(String name, DepthTestFunction depthTest) {
        RenderPipeline pipeline = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_56859}).withLocation(class_2960.method_60655((String)"recoin", (String)("pipeline/" + name))).withBlend(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE)).withDepthTestFunction(depthTest).withDepthWrite(false).withCull(false).build();
        return class_1921.method_75940((String)name, (class_12247)class_12247.method_75927((RenderPipeline)pipeline).method_75937().method_75929(65536).method_75938());
    }

    public void reset() {
        this.lightningTime = 0.0f;
        this.lastLightningNano = 0L;
        this.occlusion = 0.0f;
        this.occlusionFrom = 0.0f;
        this.occlusionTo = 0.0f;
        this.occlusionElapsed = 0.0f;
    }

    public void render(class_4587 matrices, float tickDelta, class_1309 target, float animProgress, float width) {
        if (target == null || animProgress <= 0.01f || LightningTargetESP.mc.field_1773 == null || LightningTargetESP.mc.field_1687 == null || LightningTargetESP.mc.field_1724 == null) {
            return;
        }
        long nowNano = System.nanoTime();
        float delta = this.lastLightningNano == 0L ? 0.01666f : Math.min((float)(nowNano - this.lastLightningNano) / 1.0E9f, 0.05f);
        this.lastLightningNano = nowNano;
        this.lightningTime += delta;
        class_4184 camera = LightningTargetESP.mc.field_1773.method_19418();
        class_243 camPos = camera.method_71156();
        boolean hidden = target.method_5799() || LightningTargetESP.mc.field_1687.method_17742(new class_3959(camPos, target.method_33571(), class_3959.class_3960.field_17558, class_3959.class_242.field_1348, (class_1297)LightningTargetESP.mc.field_1724)).method_17783() != class_239.class_240.field_1333;
        this.updateOcclusion(hidden, delta);
        float relX = (float)(class_3532.method_16436((double)tickDelta, (double)target.field_6038, (double)target.method_23317()) - camPos.field_1352);
        float relY = (float)(class_3532.method_16436((double)tickDelta, (double)target.field_5971, (double)target.method_23318()) - camPos.field_1351);
        float relZ = (float)(class_3532.method_16436((double)tickDelta, (double)target.field_5989, (double)target.method_23321()) - camPos.field_1350);
        float height = target.method_17682();
        float radius = target.method_17681() * 0.75f + 0.16f;
        float hurt = Math.max(0.0f, (float)target.field_6235 - tickDelta);
        float hurtFactor = (float)Math.sin((double)hurt * 0.3141592653589793) * 0.5f;
        int baseColor = ColorUtil.getClientColor();
        int col = LightningTargetESP.blendColors(baseColor, LightningTargetESP.rgba(255, 0, 0, baseColor >> 24 & 0xFF), hurtFactor);
        class_4587.class_4665 entry = matrices.method_23760();
        List<LightningSegment> segments = this.collectSegments(relX, relY, height, relZ, radius, animProgress);
        if (segments.isEmpty()) {
            return;
        }
        this.drawPass(VISIBLE_LINES, entry, segments, col, width, 1.0f);
        if (this.occlusion > 0.01f) {
            this.drawPass(OCCLUDED_LINES, entry, segments, col, width, this.occlusion);
        }
    }

    private void updateOcclusion(boolean hidden, float delta) {
        float goal;
        float f = goal = hidden ? 1.0f : 0.0f;
        if (goal != this.occlusionTo) {
            this.occlusionFrom = this.occlusion;
            this.occlusionTo = goal;
            this.occlusionElapsed = 0.0f;
        }
        this.occlusionElapsed = Math.min(this.occlusionElapsed + delta, 0.35f);
        float t = this.occlusionElapsed / 0.35f;
        float eased = t < 0.5f ? 4.0f * t * t * t : 1.0f - (float)Math.pow(-2.0f * t + 2.0f, 3.0) / 2.0f;
        this.occlusion = class_3532.method_16439((float)eased, (float)this.occlusionFrom, (float)this.occlusionTo);
    }

    private void drawPass(class_1921 layer, class_4587.class_4665 entry, List<LightningSegment> segments, int color, float width, float alphaScale) {
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27377, class_290.field_63455);
        float outerWidth = 22.0f * width;
        float outerAlpha = 0.4f * alphaScale;
        for (LightningSegment seg : segments) {
            float lineScale = Math.max(seg.a1, seg.a2);
            this.drawSegmentLine(buffer, entry, seg.x1, seg.y1, seg.z1, seg.x2, seg.y2, seg.z2, outerWidth * lineScale, LightningTargetESP.applyAlpha(color, seg.a1 * outerAlpha), LightningTargetESP.applyAlpha(color, seg.a2 * outerAlpha));
        }
        float innerWidth = 10.0f * width;
        float innerAlpha = alphaScale;
        for (LightningSegment seg : segments) {
            float lineScale = Math.max(seg.a1, seg.a2);
            this.drawSegmentLine(buffer, entry, seg.x1, seg.y1, seg.z1, seg.x2, seg.y2, seg.z2, innerWidth * lineScale, LightningTargetESP.applyAlpha(color, seg.a1 * innerAlpha), LightningTargetESP.applyAlpha(color, seg.a2 * innerAlpha));
        }
        float coreWidth = 9.0f * width;
        float coreAlpha = alphaScale;
        int coreColor = color;
        for (LightningSegment seg : segments) {
            float lineScale = Math.max(seg.a1, seg.a2);
            this.drawSegmentLine(buffer, entry, seg.x1, seg.y1, seg.z1, seg.x2, seg.y2, seg.z2, coreWidth * lineScale, LightningTargetESP.applyAlpha(coreColor, seg.a1 * coreAlpha), LightningTargetESP.applyAlpha(coreColor, seg.a2 * coreAlpha));
        }
        class_9801 built = buffer.method_60794();
        if (built != null) {
            layer.method_60895(built);
        }
    }

    private List<LightningSegment> collectSegments(float centerX, float centerY, float height, float centerZ, float radius, float alpha) {
        ArrayList<LightningSegment> segments = new ArrayList<LightningSegment>(64);
        int branchCount = 3;
        int subSteps = 3;
        float rate = this.lightningTime * 6.0f;
        int frameIndex = (int)rate;
        float frameProgress = rate - (float)frameIndex;
        float twoPi = (float)Math.PI * 2;
        for (int b = 0; b < branchCount; ++b) {
            float noiseFactor;
            float fadeOut;
            float branchPhase = this.lightningTime * 1.3f + (float)b / (float)branchCount;
            int floorPhase = (int)Math.floor(branchPhase);
            float fractPhase = branchPhase - (float)floorPhase;
            float fadeIn = class_3532.method_15363((float)(fractPhase / 0.08f), (float)0.0f, (float)1.0f);
            float weight = fadeIn * (fadeOut = 1.0f - class_3532.method_15363((float)((fractPhase - 0.42f) / 0.3f), (float)0.0f, (float)1.0f)) * (noiseFactor = class_3532.method_15363((float)(0.55f + 0.45f * LightningTargetESP.noiseInterpolate(b, 900, 0, frameIndex, frameProgress)), (float)0.0f, (float)1.0f));
            if (weight <= 0.01f) continue;
            float branchAlpha = alpha * weight;
            float rotOffset = this.lightningTime * 0.9f;
            float yJitter = height * 0.16f * (float)Math.sin(this.lightningTime * 1.7f + (float)b * 2.1f);
            float startAngle = LightningTargetESP.hashFloat(b, 11, floorPhase) * (float)Math.PI;
            float sweep = 0.6f + 0.5f * Math.abs(LightningTargetESP.hashFloat(b, 12, floorPhase));
            float dir = LightningTargetESP.hashFloat(b, 13, floorPhase) >= 0.0f ? 1.0f : -1.0f;
            float startY = centerY + height * (0.85f + 0.05f * LightningTargetESP.hashFloat(b, 14, floorPhase));
            float spanY = height * 0.67f;
            float curRadius = radius * (1.0f + 0.12f * LightningTargetESP.hashFloat(b, 15, floorPhase));
            float prevX = 0.0f;
            float prevY = 0.0f;
            float prevZ = 0.0f;
            for (int s = 0; s <= subSteps; ++s) {
                float stepFraction = (float)s / (float)subSteps;
                float angle = startAngle + rotOffset + dir * sweep * twoPi * stepFraction;
                float stepX = centerX + (float)Math.cos(angle) * curRadius;
                float stepZ = centerZ + (float)Math.sin(angle) * curRadius;
                float stepY = startY - spanY * stepFraction + yJitter;
                if (s > 0) {
                    int seed = (b * 131 + s) * 17 + floorPhase * 101 | 1;
                    this.subdivideLightningArc(segments, prevX, prevY, prevZ, stepX, stepY, stepZ, 4, branchAlpha, branchAlpha, seed, 0, frameIndex, frameProgress);
                }
                prevX = stepX;
                prevY = stepY;
                prevZ = stepZ;
            }
        }
        return segments;
    }

    private void subdivideLightningArc(List<LightningSegment> list, float x1, float y1, float z1, float x2, float y2, float z2, int depth, float a1, float a2, int seed, int branchLevel, int frameIdx, float frameProg) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (depth > 0 && len >= 1.0E-4f) {
            float pz;
            float py;
            float nx = dx / len;
            float ny = dy / len;
            float nz = dz / len;
            float perpY = Math.abs(ny) < 0.9f ? 0.0f : 1.0f;
            float perpX = Math.abs(ny) < 0.9f ? 1.0f : 0.0f;
            float px = -nz * perpX;
            float pLen = (float)Math.sqrt(px * px + (py = nz * perpY) * py + (pz = nx * perpX - ny * perpY) * pz);
            if (pLen < 1.0E-5f) {
                pLen = 1.0f;
            }
            float qx = ny * (pz /= pLen) - nz * (py /= pLen);
            float qy = nz * (px /= pLen) - nx * pz;
            float qz = nx * py - ny * px;
            float offset1 = LightningTargetESP.noiseInterpolate(seed, 0, 0, frameIdx, frameProg);
            float offset2 = LightningTargetESP.noiseInterpolate(seed, 1, 0, frameIdx, frameProg);
            float dispScale = len * 0.3f;
            float midX = (x1 + x2) * 0.5f + (px * offset1 + qx * offset2) * dispScale;
            float midY = (y1 + y2) * 0.5f + (py * offset1 + qy * offset2) * dispScale;
            float midZ = (z1 + z2) * 0.5f + (pz * offset1 + qz * offset2) * dispScale;
            float midAlpha = (a1 + a2) * 0.5f;
            this.subdivideLightningArc(list, x1, y1, z1, midX, midY, midZ, depth - 1, a1, midAlpha, seed * 2 + 7, branchLevel, frameIdx, frameProg);
            this.subdivideLightningArc(list, midX, midY, midZ, x2, y2, z2, depth - 1, midAlpha, a2, seed * 2 + 9, branchLevel, frameIdx, frameProg);
            if (branchLevel < 2 && depth >= 2 && LightningTargetESP.noiseInterpolate(seed, 2, 0, frameIdx, frameProg) > 0.45f) {
                float forkZ;
                float forkY;
                float forkDir2;
                float forkDir1 = LightningTargetESP.noiseInterpolate(seed, 3, 0, frameIdx, frameProg);
                float forkX = nx * 0.4f + px * forkDir1 + qx * (forkDir2 = LightningTargetESP.noiseInterpolate(seed, 4, 0, frameIdx, frameProg));
                float fLen = (float)Math.sqrt(forkX * forkX + (forkY = ny * 0.4f + py * forkDir1 + qy * forkDir2) * forkY + (forkZ = nz * 0.4f + pz * forkDir1 + qz * forkDir2) * forkZ);
                if (fLen < 1.0E-5f) {
                    fLen = 1.0f;
                }
                float forkDist = len * 0.65f;
                this.subdivideLightningArc(list, midX, midY, midZ, midX + forkX / fLen * forkDist, midY + forkY / fLen * forkDist, midZ + forkZ / fLen * forkDist, depth - 1, midAlpha * 0.7f, 0.0f, seed * 3 + 13, branchLevel + 1, frameIdx, frameProg);
            }
        } else {
            list.add(new LightningSegment(x1, y1, z1, x2, y2, z2, a1, a2));
        }
    }

    private void drawSegmentLine(class_287 buffer, class_4587.class_4665 entry, float x1, float y1, float z1, float x2, float y2, float z2, float lineWidth, int col1, int col2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-5f) {
            return;
        }
        Matrix4f posMat = entry.method_23761();
        buffer.method_22918((Matrix4fc)posMat, x1, y1, z1).method_39415(col1).method_60831(entry, dx / length, dy / length, dz / length).method_75298(lineWidth);
        buffer.method_22918((Matrix4fc)posMat, x2, y2, z2).method_39415(col2).method_60831(entry, dx / length, dy / length, dz / length).method_75298(lineWidth);
    }

    private static float noiseInterpolate(int a, int b, int c, int frameIndex, float frameProgress) {
        float v1 = LightningTargetESP.hashFloat(a, b, frameIndex * 4 + c);
        float v2 = LightningTargetESP.hashFloat(a, b, (frameIndex + 1) * 4 + c);
        float smooth = frameProgress * frameProgress * (3.0f - 2.0f * frameProgress);
        return v1 + (v2 - v1) * smooth;
    }

    private static float hashFloat(int a, int b, int c) {
        int hash = a * 374761393 + b * 668265263 + c * 1274126177;
        hash = (hash ^ hash >>> 13) * 1274126177;
        hash ^= hash >>> 16;
        return (float)(hash & 0xFFFF) / 32768.0f - 1.0f;
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int)(class_3532.method_15363((float)alpha, (float)0.0f, (float)1.0f) * (float)(color >> 24 & 0xFF));
        return color & 0xFFFFFF | a << 24;
    }

    private static int rgba(int r, int g, int b, int a) {
        return class_3532.method_15340((int)a, (int)0, (int)255) << 24 | class_3532.method_15340((int)r, (int)0, (int)255) << 16 | class_3532.method_15340((int)g, (int)0, (int)255) << 8 | class_3532.method_15340((int)b, (int)0, (int)255);
    }

    private static int blendColors(int c1, int c2, float ratio) {
        float f = class_3532.method_15363((float)ratio, (float)0.0f, (float)1.0f);
        float inv = 1.0f - f;
        int a = (int)((float)(c1 >> 24 & 0xFF) * inv + (float)(c2 >> 24 & 0xFF) * f);
        int r = (int)((float)(c1 >> 16 & 0xFF) * inv + (float)(c2 >> 16 & 0xFF) * f);
        int g = (int)((float)(c1 >> 8 & 0xFF) * inv + (float)(c2 >> 8 & 0xFF) * f);
        int b = (int)((float)(c1 & 0xFF) * inv + (float)(c2 & 0xFF) * f);
        return LightningTargetESP.rgba(r, g, b, a);
    }

    private static class LightningSegment {
        final float x1;
        final float y1;
        final float z1;
        final float x2;
        final float y2;
        final float z2;
        final float a1;
        final float a2;

        LightningSegment(float x1, float y1, float z1, float x2, float y2, float z2, float a1, float a2) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.a1 = a1;
            this.a2 = a2;
        }
    }
}

