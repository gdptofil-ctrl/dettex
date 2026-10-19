/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_638
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package ru.recoin.implement.features.modules.render.esp;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_638;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.math.MathUtil;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.pipeline.HitBubblePipeline;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;

public final class CrystalTargetESP
implements QuickImports {
    private static final Vector3f[] CRYSTAL_VERTICES = new Vector3f[]{new Vector3f(0.0f, 1.5f, 0.0f), new Vector3f(0.0f, -1.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f)};
    private static final int[][] CRYSTAL_FACES = new int[][]{{0, 4, 2}, {0, 3, 4}, {0, 5, 3}, {0, 2, 5}, {1, 2, 4}, {1, 4, 3}, {1, 3, 5}, {1, 5, 2}};
    private static final float[] CRYSTAL_BRIGHTNESS = new float[]{1.0f, 0.8f, 0.6f, 0.9f, 0.7f, 0.5f, 0.4f, 0.6f};
    private final HitBubblePipeline distortionPipeline = new HitBubblePipeline();
    private class_638 lastWorld;

    public void reset() {
        this.distortionPipeline.close();
        this.lastWorld = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(class_4587 matrices, class_1309 target, float anim, float distortionStrength, boolean enableDistortion) {
        float[][] glowPasses;
        if (target == null || anim <= 0.001f || CrystalTargetESP.mc.field_1773 == null || CrystalTargetESP.mc.field_1724 == null) {
            return;
        }
        if (CrystalTargetESP.mc.field_1687 != this.lastWorld) {
            this.distortionPipeline.close();
            this.lastWorld = CrystalTargetESP.mc.field_1687;
        }
        class_4184 camera = CrystalTargetESP.mc.field_1773.method_19418();
        class_243 cameraPos = camera.method_71156();
        class_243 targetPos = MathUtil.interpolate((class_1297)target);
        class_243 targetCenter = target.method_73189().method_1031(0.0, (double)target.method_17682() / 2.0, 0.0);
        float moving = (float)(System.currentTimeMillis() % 360000L) / 2.5f + anim;
        float radius = target.method_17681() * 1.5f;
        float scale = 1.25f - 0.5f * anim;
        int color = ColorUtil.multAlpha(ColorUtil.getClientColor(), anim);
        if (enableDistortion && distortionStrength > 0.01f) {
            class_287 bubbleBuffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
            float crystalDistortionRadius = 0.24f * anim;
            int bubbleAlpha = Math.round(218.0f * anim);
            int bubbleColor = color & 0xFFFFFF | bubbleAlpha << 24;
            for (int i = 0; i < 360; i += 20) {
                double angle = Math.toRadians((float)i + moving * 0.3f);
                float x = (float)Math.sin(angle) * radius * scale;
                float z = (float)Math.cos(angle) * radius * scale;
                float y = (float)((double)0.1f + (double)target.method_17682() * Math.abs(Math.sin(i)));
                class_243 crystalPos = targetPos.method_1031((double)x, (double)y, (double)z);
                matrices.method_22903();
                try {
                    matrices.method_22904(crystalPos.field_1352 - cameraPos.field_1352, crystalPos.field_1351 - cameraPos.field_1351, crystalPos.field_1350 - cameraPos.field_1350);
                    matrices.method_22907((Quaternionfc)camera.method_23767());
                    Matrix4f matrix = matrices.method_23760().method_23761();
                    float r = crystalDistortionRadius;
                    bubbleBuffer.method_22918((Matrix4fc)matrix, -r, -r, 0.0f).method_22913(-1.0f, -1.0f).method_39415(bubbleColor);
                    bubbleBuffer.method_22918((Matrix4fc)matrix, r, -r, 0.0f).method_22913(1.0f, -1.0f).method_39415(bubbleColor);
                    bubbleBuffer.method_22918((Matrix4fc)matrix, r, r, 0.0f).method_22913(1.0f, 1.0f).method_39415(bubbleColor);
                    bubbleBuffer.method_22918((Matrix4fc)matrix, -r, r, 0.0f).method_22913(-1.0f, 1.0f).method_39415(bubbleColor);
                    continue;
                }
                finally {
                    matrices.method_22909();
                }
            }
            class_9801 builtBubble = bubbleBuffer.method_60794();
            if (builtBubble != null) {
                this.distortionPipeline.draw(builtBubble, distortionStrength);
            }
        }
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        class_287 crystals = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27379, class_290.field_1576);
        for (int i = 0; i < 360; i += 20) {
            double angle = Math.toRadians((float)i + moving * 0.3f);
            float x = (float)Math.sin(angle) * radius * scale;
            float z = (float)Math.cos(angle) * radius * scale;
            float y = (float)((double)0.1f + (double)target.method_17682() * Math.abs(Math.sin(i)));
            matrices.method_22903();
            class_243 crystalPos = targetPos.method_1031((double)x, (double)y, (double)z);
            matrices.method_22904(crystalPos.field_1352 - cameraPos.field_1352, crystalPos.field_1351 - cameraPos.field_1351, crystalPos.field_1350 - cameraPos.field_1350);
            Vector3f direction = new Vector3f((float)(targetCenter.field_1352 - crystalPos.field_1352), (float)(targetCenter.field_1351 - crystalPos.field_1351), (float)(targetCenter.field_1350 - crystalPos.field_1350)).normalize();
            matrices.method_22907((Quaternionfc)new Quaternionf().rotationTo((Vector3fc)new Vector3f(0.0f, 1.0f, 0.0f), (Vector3fc)direction));
            this.renderCrystalMesh(matrices, crystals, 0.0f, 0.0f, 0.0f, 0.1f, color);
            matrices.method_22909();
        }
        BufferRenderer.drawWithGlobalProgram(crystals.method_60800());
        LegacyRenderSystem.setShaderTexture(0, class_2960.method_60654((String)"textures/glow.png"));
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        for (float[] pass : glowPasses = new float[][]{{0.28f * anim, anim * 0.45f}, {0.44f * anim, anim * 0.2f}}) {
            float glowSize = pass[0];
            int bloomColor = ColorUtil.multAlpha(ColorUtil.getClientColor(), pass[1]);
            class_287 glows = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
            for (int i = 0; i < 360; i += 20) {
                double angle = Math.toRadians((float)i + moving * 0.3f);
                float x = (float)Math.sin(angle) * radius * scale;
                float z = (float)Math.cos(angle) * radius * scale;
                float y = (float)((double)0.1f + (double)target.method_17682() * Math.abs(Math.sin(i)));
                class_243 crystalPos = targetPos.method_1031((double)x, (double)y, (double)z);
                matrices.method_22903();
                matrices.method_22904(crystalPos.field_1352 - cameraPos.field_1352, crystalPos.field_1351 - cameraPos.field_1351, crystalPos.field_1350 - cameraPos.field_1350);
                matrices.method_22907((Quaternionfc)camera.method_23767());
                Matrix4f matrix = matrices.method_23760().method_23761();
                float half = glowSize / 2.0f;
                glows.method_22918((Matrix4fc)matrix, -half, half, 0.0f).method_22913(0.0f, 1.0f).method_39415(bloomColor);
                glows.method_22918((Matrix4fc)matrix, half, half, 0.0f).method_22913(1.0f, 1.0f).method_39415(bloomColor);
                glows.method_22918((Matrix4fc)matrix, half, -half, 0.0f).method_22913(1.0f, 0.0f).method_39415(bloomColor);
                glows.method_22918((Matrix4fc)matrix, -half, -half, 0.0f).method_22913(0.0f, 0.0f).method_39415(bloomColor);
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

    private void renderCrystalMesh(class_4587 matrices, class_287 buffer, float x, float y, float z, float size, int color) {
        matrices.method_22903();
        matrices.method_46416(x, y, z);
        matrices.method_22905(size, size, size);
        Matrix4f matrix = matrices.method_23760().method_23761();
        for (int i = 0; i < CRYSTAL_FACES.length; ++i) {
            int[] face = CRYSTAL_FACES[i];
            int shaded = this.applyBrightness(color, CRYSTAL_BRIGHTNESS[i]);
            Vector3f v1 = CRYSTAL_VERTICES[face[0]];
            Vector3f v2 = CRYSTAL_VERTICES[face[1]];
            Vector3f v3 = CRYSTAL_VERTICES[face[2]];
            buffer.method_22918((Matrix4fc)matrix, v1.x, v1.y, v1.z).method_39415(shaded);
            buffer.method_22918((Matrix4fc)matrix, v2.x, v2.y, v2.z).method_39415(shaded);
            buffer.method_22918((Matrix4fc)matrix, v3.x, v3.y, v3.z).method_39415(shaded);
        }
        matrices.method_22909();
    }

    private int applyBrightness(int color, float brightness) {
        int alpha = ColorUtil.alpha(color);
        int red = Math.min(255, Math.max(0, (int)((float)ColorUtil.red(color) * brightness)));
        int green = Math.min(255, Math.max(0, (int)((float)ColorUtil.green(color) * brightness)));
        int blue = Math.min(255, Math.max(0, (int)((float)ColorUtil.blue(color) * brightness)));
        return ColorUtil.getColor(red, green, blue, alpha);
    }
}

