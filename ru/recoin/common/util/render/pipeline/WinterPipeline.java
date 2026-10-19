/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.SourceFactor
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_12137
 *  net.minecraft.class_276
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.lwjgl.system.MemoryUtil
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_276;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public final class WinterPipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/winter");
    private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/winter")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("Uniforms", class_10789.field_60031).withSampler("Sampler0").withSampler("Sampler1").withBlend(REPLACE_BLEND).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final int BUFFER_SIZE = 544;
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertex;
    private ByteBuffer data;
    private GpuTexture copyTexture;
    private GpuTextureView copyView;
    private GpuTexture copyDepthTexture;
    private GpuTextureView copyDepthView;
    private int lastWidth;
    private int lastHeight;

    public void draw(Matrix4f proj, Matrix4f invProj, Matrix4f viewRot, Vector3f camPos, float fov, float time, float snowAmount, float sparkleIntensity, float darkening, float vignette, float snowThreshold, float coldGrading) {
        this.draw(proj, invProj, viewRot, camPos, fov, time, snowAmount, sparkleIntensity, darkening, vignette, snowThreshold, coldGrading, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, null, 0);
    }

    public void draw(Matrix4f proj, Matrix4f invProj, Matrix4f viewRot, Vector3f camPos, float fov, float time, float snowAmount, float sparkleIntensity, float darkening, float vignette, float snowThreshold, float coldGrading, float fogStart, float fogEnd, float fogActive, float fogR, float fogG, float fogB, float fogA) {
        this.draw(proj, invProj, viewRot, camPos, fov, time, snowAmount, sparkleIntensity, darkening, vignette, snowThreshold, coldGrading, fogStart, fogEnd, fogActive, fogR, fogG, fogB, fogA, null, 0);
    }

    public void draw(Matrix4f proj, Matrix4f invProj, Matrix4f viewRot, Vector3f camPos, float fov, float time, float snowAmount, float sparkleIntensity, float darkening, float vignette, float snowThreshold, float coldGrading, float fogStart, float fogEnd, float fogActive, float fogR, float fogG, float fogB, float fogA, float[] playersData, int playerCount) {
        class_310 client = class_310.method_1551();
        class_276 fb = client.method_1522();
        if (fb == null || fb.method_30277() == null) {
            return;
        }
        this.ensure();
        int w = fb.field_1482;
        int h = fb.field_1481;
        this.ensureTextures(w, h);
        this.data.clear();
        invProj.get(this.data);
        this.data.position(this.data.position() + 64);
        proj.get(this.data);
        this.data.position(this.data.position() + 64);
        viewRot.get(this.data);
        this.data.position(this.data.position() + 64);
        this.data.putFloat(camPos.x).putFloat(camPos.y).putFloat(camPos.z).putFloat(fov);
        this.data.putFloat(w).putFloat(h).putFloat(0.0f).putFloat(0.0f);
        this.data.putFloat(snowAmount).putFloat(sparkleIntensity).putFloat(time).putFloat(darkening);
        this.data.putFloat(vignette).putFloat(snowThreshold).putFloat(coldGrading).putFloat(playerCount);
        this.data.putFloat(fogStart).putFloat(fogEnd).putFloat(fogActive).putFloat(0.0f);
        this.data.putFloat(fogR).putFloat(fogG).putFloat(fogB).putFloat(fogA);
        for (int i = 0; i < 64; ++i) {
            this.data.putFloat(playersData != null && i < playersData.length ? playersData[i] : 0.0f);
        }
        this.data.flip();
        int sz = this.data.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)sz) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:winter_uniform", 136, (long)sz);
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(fb.method_30277(), this.copyTexture, 0, 0, 0, 0, 0, w, h);
        if (fb.method_30278() != null && this.copyDepthTexture != null) {
            encoder.copyTextureToTexture(fb.method_30278(), this.copyDepthTexture, 0, 0, 0, 0, 0, w, h);
        }
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.data);
        class_12137 linear = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        class_12137 nearest = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:winter_pass", fb.method_71639(), OptionalInt.empty());){
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.dummyVertex);
            pass.bindTexture("Sampler0", this.copyView, linear);
            pass.bindTexture("Sampler1", this.copyDepthView != null ? this.copyDepthView : this.copyView, nearest);
            pass.setUniform("Uniforms", this.uniformBuffer);
            pass.draw(0, 6);
        }
    }

    private void ensure() {
        if (this.data != null) {
            return;
        }
        this.data = MemoryUtil.memAlloc((int)544);
        ByteBuffer d = MemoryUtil.memAlloc((int)4).putInt(0).flip();
        this.dummyVertex = RenderSystem.getDevice().createBuffer(() -> "recoin:winter_vertex", 32, d);
        MemoryUtil.memFree((Buffer)d);
    }

    private void ensureTextures(int w, int h) {
        if (this.copyTexture != null && this.lastWidth == w && this.lastHeight == h) {
            return;
        }
        if (this.copyView != null) {
            this.copyView.close();
            this.copyView = null;
        }
        if (this.copyTexture != null) {
            this.copyTexture.close();
            this.copyTexture = null;
        }
        if (this.copyDepthView != null) {
            this.copyDepthView.close();
            this.copyDepthView = null;
        }
        if (this.copyDepthTexture != null) {
            this.copyDepthTexture.close();
            this.copyDepthTexture = null;
        }
        this.copyTexture = RenderSystem.getDevice().createTexture(() -> "recoin:winter_copy", 5, TextureFormat.RGBA8, w, h, 1, 1);
        this.copyView = RenderSystem.getDevice().createTextureView(this.copyTexture);
        this.copyDepthTexture = RenderSystem.getDevice().createTexture(() -> "recoin:winter_depth", 5, TextureFormat.DEPTH32, w, h, 1, 1);
        this.copyDepthView = RenderSystem.getDevice().createTextureView(this.copyDepthTexture);
        this.lastWidth = w;
        this.lastHeight = h;
    }
}

