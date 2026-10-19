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
 *  net.minecraft.class_3532
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package ru.recoin.implement.features.modules.render.esp;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import ru.recoin.common.QuickImports;
import ru.recoin.common.util.color.ColorUtil;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.compat.render.BufferRenderer;
import ru.recoin.compat.render.ShaderProgramKeys;
import ru.recoin.implement.features.modules.render.Hud;
import ru.recoin.implement.features.modules.render.esp.SwordMesh;

public final class SwordTargetESP
implements QuickImports {
    private static final int SWORD_COUNT = 7;
    private final Matrix4f[] swordMatrices = new Matrix4f[7];
    private final int[] swordColors = new int[7];
    private final double[] swordX = new double[7];
    private final double[] swordY = new double[7];
    private final double[] swordZ = new double[7];

    public SwordTargetESP() {
        for (int i = 0; i < 7; ++i) {
            this.swordMatrices[i] = new Matrix4f();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(class_4587 matrices, float tickDelta, class_1309 target, float appear, float hurt) {
        if (target == null || appear <= 0.001f || SwordTargetESP.mc.field_1773 == null) {
            return;
        }
        class_4184 camera = SwordTargetESP.mc.field_1773.method_19418();
        if (camera == null) {
            return;
        }
        class_243 cameraPos = camera.method_71156();
        double centerX = class_3532.method_16436((double)tickDelta, (double)target.field_6038, (double)target.method_23317());
        double centerY = class_3532.method_16436((double)tickDelta, (double)target.field_5971, (double)target.method_23318()) + (double)target.method_17682() / 1.4 - 0.06;
        double centerZ = class_3532.method_16436((double)tickDelta, (double)target.field_5989, (double)target.method_23321());
        double radius = Math.max(target.method_5829().method_17939(), target.method_5829().method_17941()) * 0.5 + 0.2;
        double time = (double)System.nanoTime() / 1.0E9;
        double orbit = time * 2.1 % (Math.PI * 2);
        float pulse = 1.0f + (float)Math.sin(time * 4.0) * 0.025f;
        float spawnScale = 0.78f + 0.22f * appear;
        int nearestSword = 0;
        double nearestDistance = Double.MAX_VALUE;
        for (int i = 0; i < 7; ++i) {
            double angle = orbit + Math.PI * 2 * (double)i / 7.0;
            double orbitRadius = radius + Math.sin(time * 3.0 + (double)i * 0.7) * 0.025;
            this.swordX[i] = centerX + Math.cos(angle) * orbitRadius;
            this.swordY[i] = centerY + Math.sin(time * 5.0 + (double)i * 0.65) * 0.032;
            this.swordZ[i] = centerZ + Math.sin(angle) * orbitRadius;
            double distance = cameraPos.method_1028(this.swordX[i], this.swordY[i], this.swordZ[i]);
            if (!(distance < nearestDistance)) continue;
            nearestDistance = distance;
            nearestSword = i;
        }
        Hud hud = Hud.getInstance();
        int primary = hud.themeGradientStart();
        int secondary = hud.themeGradientEnd();
        Matrix4f baseMatrix = matrices.method_23760().method_23761();
        for (int i = 0; i < 7; ++i) {
            double angle = orbit + Math.PI * 2 * (double)i / 7.0;
            float yaw = (float)(-Math.atan2(Math.cos(angle), -Math.sin(angle)));
            float roll = (float)Math.toRadians(Math.sin(time * 4.0 + (double)i) * 2.5);
            float hit = i == nearestSword ? class_3532.method_15363((float)hurt, (float)0.0f, (float)1.0f) : 0.0f;
            float scale = 0.12f * pulse * spawnScale * (1.0f + hit * 0.472f);
            float colorPhase = (0.5f + 0.5f * (float)Math.sin(time * 4.2 + (double)i * 0.9)) * 0.48f;
            int color = ColorUtil.overCol(primary, secondary, colorPhase);
            this.swordColors[i] = ColorUtil.overCol(color, ColorUtil.getColor(255, 0, 60), hit);
            this.swordMatrices[i].set((Matrix4fc)baseMatrix).translate((float)(this.swordX[i] - cameraPos.field_1352), (float)(this.swordY[i] - cameraPos.field_1351), (float)(this.swordZ[i] - cameraPos.field_1350)).rotateY(yaw).rotateX(-1.5707964f).rotateZ(roll).scale(scale, -scale, scale);
        }
        LegacyRenderSystem.enableBlend();
        LegacyRenderSystem.defaultBlendFunc();
        LegacyRenderSystem.disableCull();
        LegacyRenderSystem.disableDepthTest();
        LegacyRenderSystem.depthMask(false);
        LegacyRenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        try {
            class_287 fill = class_289.method_1348().method_60827(VertexFormat.class_5596.field_27379, class_290.field_1576);
            for (int i = 0; i < 7; ++i) {
                SwordTargetESP.emitMesh(fill, this.swordMatrices[i], SwordMesh.TRIANGLES, ColorUtil.multAlpha(this.swordColors[i], appear * 0.5f));
            }
            BufferRenderer.drawWithGlobalProgram(fill.method_60800());
            class_287 outline = class_289.method_1348().method_60827(VertexFormat.class_5596.field_29344, class_290.field_1576);
            for (int i = 0; i < 7; ++i) {
                SwordTargetESP.emitMesh(outline, this.swordMatrices[i], SwordMesh.OUTLINE, ColorUtil.multAlpha(ColorUtil.WHITE, appear * 0.8f));
            }
            BufferRenderer.drawWithGlobalProgram(outline.method_60800());
        }
        finally {
            LegacyRenderSystem.depthMask(true);
            LegacyRenderSystem.enableDepthTest();
            LegacyRenderSystem.enableCull();
            LegacyRenderSystem.defaultBlendFunc();
            LegacyRenderSystem.disableBlend();
        }
    }

    private static void emitMesh(class_287 buffer, Matrix4f matrix, int[] indices, int color) {
        float[] positions = SwordMesh.POSITIONS;
        for (int vertex : indices) {
            int offset = vertex * 3;
            buffer.method_22918((Matrix4fc)matrix, positions[offset], positions[offset + 1], positions[offset + 2]).method_39415(color);
        }
    }
}

