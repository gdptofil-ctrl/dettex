/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_287
 *  net.minecraft.class_290
 *  net.minecraft.class_310
 *  net.minecraft.class_6367
 */
package ru.recoin.common.util.render.shader;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_6367;
import ru.recoin.common.util.render.LegacyRenderSystem;
import ru.recoin.common.util.render.shader.Shader;
import ru.recoin.compat.render.BufferRenderer;

public class ShaderHelper {
    private static class_310 mc = class_310.method_1551();
    private static Shader chromaShader;
    private static Shader solidShader;
    private static Shader balatroShader;
    private static Shader smokeShader;
    private static Shader stripesShader;
    private static Shader glowShader;
    private static Shader glassShader;
    private static Shader snowShader;
    private static Shader blendShader;
    private static Shader camouflageShader;
    private static Shader mirrorShader;
    private static Shader linesShader;
    private static class_6367 copyFbo;
    private static class_6367 fbo1;
    private static class_6367 fbo2;
    private static class_6367 effectFbo;
    private static class_6367 solidFbo;
    private static class_6367 shader1Fbo;
    private static class_6367 shader2Fbo;
    private static boolean initialized;

    public static void initShadersIfNeeded() {
        if (initialized) {
            return;
        }
        chromaShader = ShaderHelper.loadShader("chroma");
        solidShader = ShaderHelper.loadShader("solid");
        balatroShader = ShaderHelper.loadShader("balatro");
        smokeShader = ShaderHelper.loadShader("smoke");
        stripesShader = ShaderHelper.loadShader("stripes");
        glowShader = ShaderHelper.loadShader("glow");
        glassShader = ShaderHelper.loadShader("glass");
        snowShader = ShaderHelper.loadShader("snow");
        blendShader = ShaderHelper.loadShader("blend");
        camouflageShader = ShaderHelper.loadShader("camouflage");
        mirrorShader = ShaderHelper.loadShader("mirror");
        linesShader = ShaderHelper.loadShader("lines");
        initialized = true;
    }

    private static Shader loadShader(String name) {
        try {
            return new Shader("hand", name);
        }
        catch (Exception e) {
            System.err.println("Failed to initialize hand shader: " + name);
            e.printStackTrace();
            return null;
        }
    }

    public static void checkFramebuffers() {
        if (mc == null || mc.method_22683() == null) {
            return;
        }
        int width = mc.method_22683().method_4489();
        int height = mc.method_22683().method_4506();
        if (copyFbo == null || ShaderHelper.copyFbo.field_1482 != width || ShaderHelper.copyFbo.field_1481 != height) {
            if (copyFbo != null) {
                copyFbo.method_1238();
                fbo1.method_1238();
                fbo2.method_1238();
                effectFbo.method_1238();
                solidFbo.method_1238();
                if (shader1Fbo != null) {
                    shader1Fbo.method_1238();
                }
                if (shader2Fbo != null) {
                    shader2Fbo.method_1238();
                }
            }
            copyFbo = new class_6367("recoin_copy", width, height, true);
            fbo1 = new class_6367("recoin_shader_1", width, height, true);
            fbo2 = new class_6367("recoin_shader_2", width, height, true);
            effectFbo = new class_6367("recoin_effect", width, height, true);
            solidFbo = new class_6367("recoin_solid", width, height, true);
            shader1Fbo = new class_6367("recoin_shader_pass_1", width, height, true);
            shader2Fbo = new class_6367("recoin_shader_pass_2", width, height, true);
        }
    }

    public static void drawFullScreenQuad() {
        LegacyRenderSystem.assertOnRenderThread();
        class_287 bufferBuilder = LegacyRenderSystem.renderThreadTesselator().method_60827(VertexFormat.class_5596.field_27382, class_290.field_1592);
        bufferBuilder.method_22912(-1.0f, -1.0f, 0.0f);
        bufferBuilder.method_22912(1.0f, -1.0f, 0.0f);
        bufferBuilder.method_22912(1.0f, 1.0f, 0.0f);
        bufferBuilder.method_22912(-1.0f, 1.0f, 0.0f);
        BufferRenderer.draw(bufferBuilder.method_60800());
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static Shader getChromaShader() {
        return chromaShader;
    }

    public static Shader getSolidShader() {
        return solidShader;
    }

    public static Shader getBalatroShader() {
        return balatroShader;
    }

    public static Shader getSmokeShader() {
        return smokeShader;
    }

    public static class_6367 getCopyFbo() {
        return copyFbo;
    }

    public static class_6367 getFbo1() {
        return fbo1;
    }

    public static class_6367 getFbo2() {
        return fbo2;
    }

    public static class_6367 getEffectFbo() {
        return effectFbo;
    }

    public static class_6367 getSolidFbo() {
        return solidFbo;
    }

    public static Shader getStripesShader() {
        return stripesShader;
    }

    public static Shader getGlowShader() {
        return glowShader;
    }

    public static Shader getGlassShader() {
        return glassShader;
    }

    public static Shader getSnowShader() {
        return snowShader;
    }

    public static Shader getBlendShader() {
        return blendShader;
    }

    public static Shader getCamouflageShader() {
        return camouflageShader;
    }

    public static Shader getMirrorShader() {
        return mirrorShader;
    }

    public static Shader getLinesShader() {
        return linesShader;
    }

    public static class_6367 getShader1Fbo() {
        return shader1Fbo;
    }

    public static class_6367 getShader2Fbo() {
        return shader2Fbo;
    }

    static {
        initialized = false;
    }
}

