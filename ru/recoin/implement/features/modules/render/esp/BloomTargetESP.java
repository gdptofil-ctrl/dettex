/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_1309
 *  net.minecraft.class_243
 *  net.minecraft.class_287
 *  net.minecraft.class_289
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_7833
 *  net.minecraft.class_9801
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package ru.recoin.implement.features.modules.render.esp;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Random;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.GlStateManager;
import ru.recoin.compat.render.ShaderProgramKeys;

public final class BloomTargetESP
implements QuickImports {
    public static final int MAX_PARTICLES = 64;
    private static final float PARTICLE_SIZE = 0.2f;
    private static final float PARTICLE_SPEED = 1.0f;
    private static final class_2960 BLOOM_TEXTURE = class_2960.method_60655((String)"minecraft", (String)"textures/bloom.png");
    private static final BloomSeed[] SEEDS = BloomTargetESP.createSeeds();
    private final Matrix4f particleMatrix = new Matrix4f();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(class_4587 matrices, float tickDelta, class_1309 target, float appear, float hurt, int count) {
        if (target == null || appear <= 0.001f || BloomTargetESP.mc.field_1773 == null) {
            return;
        }
        class_4184 camera = BloomTargetESP.mc.field_1773.method_19418();
        if (camera == null) {
            return;
        }
        class_243 cameraPos = camera.method_71156();
        class_243 center = new class_243(class_3532.method_16436((double)tickDelta, (double)target.field_6038, (double)target.method_23317()), class_3532.method_16436((double)tickDelta, (double)target.field_5971, (double)target.method_23318()), class_3532.method_16436((double)tickDelta, (double)target.field_5989, (double)target.method_23321()));
        double time = (double)System.currentTimeMillis() / 1000.0 * 1.0;
        int particleCount = class_3532.method_15340((int)count, (int)1, (int)64);
        float entityHeight = target.method_17682();
        float entityWidth = target.method_17681();
        int theme = ColorUtil.overCol(ColorUtil.getClientColor(), ColorUtil.getColor(255, 85, 105), hurt);
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.blendFunc((Object)GlStateManager.SrcFactor.SRC_ALPHA, (Object)GlStateManager.DstFactor.ONE);
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        LegacyRenderSystem.setShaderTexture(0, BLOOM_TEXTURE);
        class_287 buffer = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1575);
        float yaw = camera.method_19330();
        float pitch = camera.method_19329();
        Quaternionf yawRotation = class_7833.field_40716.rotationDegrees(-yaw);
        Quaternionf pitchRotation = class_7833.field_40714.rotationDegrees(pitch);
        Matrix4f baseMatrix = matrices.method_23760().method_23761();
        for (int i = 0; i < particleCount; ++i) {
            BloomSeed seed = SEEDS[i];
            float rawHeight = (float)(((double)seed.height + time * (double)seed.verticalSpeed * 0.22) % 1.15);
            if (rawHeight < 0.0f) {
                rawHeight += 1.15f;
            }
            float heightFade = rawHeight < 0.12f ? rawHeight / 0.12f : (rawHeight > 0.92f ? Math.max(0.0f, (1.15f - rawHeight) / 0.23f) : 1.0f);
            double angle = (double)seed.angle + time * (double)seed.orbitSpeed + Math.sin(time * (double)seed.radialFrequency + (double)seed.pulsePhase) * 0.35;
            double radius = (double)entityWidth * 0.65 * (double)seed.radius + Math.sin(time * (double)seed.radialFrequency * 1.4 + (double)seed.pulsePhase) * (double)seed.radialAmplitude;
            double worldX = center.field_1352 + Math.cos(angle) * radius;
            double worldY = center.field_1351 + (double)(rawHeight * entityHeight);
            double worldZ = center.field_1350 + Math.sin(angle) * radius;
            float pulse = 0.78f + 0.32f * (float)Math.sin(time * (double)seed.pulseSpeed + (double)seed.pulsePhase);
            float particleSize = 0.2f * seed.sizeFactor * pulse * appear;
            float alpha = appear * heightFade * (0.6f + 0.4f * (float)Math.cos(time * (double)seed.pulseSpeed + (double)seed.pulsePhase));
            if (particleSize <= 0.01f || alpha <= 0.01f) continue;
            int color = ColorUtil.multAlpha(theme, alpha);
            int red = ColorUtil.red(color);
            int green = ColorUtil.green(color);
            int blue = ColorUtil.blue(color);
            int outerAlpha = Math.round((float)ColorUtil.alpha(color) * 0.55f);
            int coreAlpha = Math.round((float)ColorUtil.alpha(color) * 0.9f);
            float rotation = seed.spin + (float)(time * (double)seed.spinSpeed);
            this.particleMatrix.set((Matrix4fc)baseMatrix).translate((float)(worldX - cameraPos.field_1352), (float)(worldY - cameraPos.field_1351), (float)(worldZ - cameraPos.field_1350)).rotate((Quaternionfc)yawRotation).rotate((Quaternionfc)pitchRotation).rotate((Quaternionfc)class_7833.field_40718.rotationDegrees(rotation));
            BloomTargetESP.emitQuad(buffer, this.particleMatrix, particleSize * 1.55f, red, green, blue, outerAlpha);
            BloomTargetESP.emitQuad(buffer, this.particleMatrix, particleSize, red, green, blue, coreAlpha);
        }
        try {
            class_9801 builtBuffer = buffer.method_60794();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }
        }
        finally {
            LegacyRenderSystem.depthMask(true);
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.defaultBlendFunc();
            LegacyRenderSystem.disableBlend();
        }
    }

    private static void emitQuad(class_287 buffer, Matrix4f matrix, float size, int red, int green, int blue, int alpha) {
        float half = size * 0.5f;
        buffer.method_22918((Matrix4fc)matrix, -half, -half, 0.0f).method_22913(0.0f, 1.0f).method_1336(red, green, blue, alpha);
        buffer.method_22918((Matrix4fc)matrix, half, -half, 0.0f).method_22913(1.0f, 1.0f).method_1336(red, green, blue, alpha);
        buffer.method_22918((Matrix4fc)matrix, half, half, 0.0f).method_22913(1.0f, 0.0f).method_1336(red, green, blue, alpha);
        buffer.method_22918((Matrix4fc)matrix, -half, half, 0.0f).method_22913(0.0f, 0.0f).method_1336(red, green, blue, alpha);
    }

    private static BloomSeed[] createSeeds() {
        BloomSeed[] seeds = new BloomSeed[64];
        for (int i = 0; i < seeds.length; ++i) {
            Random random = new Random((long)i * 2654435761L + 987654321L);
            seeds[i] = new BloomSeed((float)i / 64.0f + (random.nextFloat() - 0.5f) * 0.06f, 0.45f + random.nextFloat() * 0.55f, random.nextFloat() * ((float)Math.PI * 2), (i % 2 == 0 ? 1.0f : -1.0f) * (0.8f + random.nextFloat() * 1.2f), 0.25f + random.nextFloat() * 0.45f, 2.0f + random.nextFloat() * 3.5f, random.nextFloat() * ((float)Math.PI * 2), (random.nextFloat() - 0.5f) * 90.0f, random.nextFloat() * 360.0f, 0.75f + random.nextFloat() * 0.5f, 1.2f + random.nextFloat() * 2.0f, 0.04f + random.nextFloat() * 0.09f);
        }
        return seeds;
    }

    private record BloomSeed(float height, float radius, float angle, float orbitSpeed, float verticalSpeed, float pulseSpeed, float pulsePhase, float spinSpeed, float spin, float sizeFactor, float radialFrequency, float radialAmplitude) {
    }
}

