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

public final class WetWorldPipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/wet_world");
    private static final BlendFunction REPLACE_BLEND = new BlendFunction(SourceFactor.ONE, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ZERO);
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/wet_world")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("Uniforms", class_10789.field_60031).withSampler("Sampler0").withSampler("Sampler1").withBlend(REPLACE_BLEND).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final int BUFFER_SIZE = 288;
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertex;
    private ByteBuffer data;
    private GpuTexture copyTexture;
    private GpuTextureView copyView;
    private int lastWidth;
    private int lastHeight;

    public void draw(Matrix4f proj, Matrix4f invProj, Matrix4f viewRot, Vector3f camPos, float fov, float time) {
        class_310 client = class_310.method_1551();
        class_276 fb = client.method_1522();
        if (fb == null || fb.method_30277() == null || fb.method_71640() == null || fb.field_1482 <= 0 || fb.field_1481 <= 0) {
            return;
        }
        this.ensure();
        int w = fb.field_1482;
        int h = fb.field_1481;
        this.ensureTextures(w, h);
        float fogStart = 144.0f;
        float fogEnd = 192.0f;
        if (client.field_1690 != null) {
            int viewDist = client.field_1690.method_38521();
            fogEnd = (float)viewDist * 16.0f;
            fogStart = fogEnd * 0.75f;
        }
        this.data.clear();
        invProj.get(this.data);
        this.data.position(this.data.position() + 64);
        proj.get(this.data);
        this.data.position(this.data.position() + 64);
        viewRot.get(this.data);
        this.data.position(this.data.position() + 64);
        this.data.putFloat(camPos.x).putFloat(camPos.y).putFloat(camPos.z).putFloat(fov);
        this.data.putFloat(w).putFloat(h).putFloat(0.0f).putFloat(0.0f);
        this.data.putFloat(0.85f).putFloat(0.3f).putFloat(time).putFloat(0.0f);
        this.data.putFloat(2.0f).putFloat(1.0f).putFloat(1.0f).putFloat(0.0f);
        this.data.putFloat(fogStart).putFloat(fogEnd).putFloat(0.0f).putFloat(0.0f);
        this.data.putFloat(0.0f).putFloat(0.0f).putFloat(0.0f).putFloat(0.0f);
        this.data.flip();
        int sz = this.data.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)sz) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "recoin:wet_world_uniform", 136, (long)sz);
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(fb.method_30277(), this.copyTexture, 0, 0, 0, 0, 0, w, h);
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.data);
        class_12137 linear = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        class_12137 nearest = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:wet_world_pass", fb.method_71639(), OptionalInt.empty());){
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.dummyVertex);
            pass.bindTexture("Sampler0", this.copyView, linear);
            pass.bindTexture("Sampler1", fb.method_71640(), nearest);
            pass.setUniform("Uniforms", this.uniformBuffer);
            pass.draw(0, 3);
        }
    }

    private void ensure() {
        if (this.data != null) {
            return;
        }
        this.data = MemoryUtil.memAlloc((int)288);
        ByteBuffer d = MemoryUtil.memAlloc((int)4).putInt(0).flip();
        this.dummyVertex = RenderSystem.getDevice().createBuffer(() -> "recoin:wet_world_vertex", 32, d);
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
        this.copyTexture = RenderSystem.getDevice().createTexture(() -> "recoin:wet_world_copy", 5, TextureFormat.RGBA8, w, h, 1, 1);
        this.copyView = RenderSystem.getDevice().createTextureView(this.copyTexture);
        this.lastWidth = w;
        this.lastHeight = h;
    }
}

