/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10789
 *  net.minecraft.class_10799
 *  net.minecraft.class_12137
 *  net.minecraft.class_290
 *  net.minecraft.class_2960
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryUtil
 */
package ru.recoin.common.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_12137;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;
import ru.recoin.common.util.render.pipeline.MaskCoveragePipeline;

public final class ShaderEspGlowPipeline {
    private static final class_2960 SHADER = class_2960.method_60655((String)"minecraft", (String)"core/recoin_shader_esp_glow");
    private static final RenderPipeline PIPELINE = class_10799.method_67887((RenderPipeline)RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withLocation(class_2960.method_60655((String)"recoin", (String)"pipeline/shader_esp_glow")).withVertexShader(SHADER).withFragmentShader(SHADER).withVertexFormat(class_290.field_60033, VertexFormat.class_5596.field_27379).withUniform("GlowData", class_10789.field_60031).withSampler("MaskSampler").withSampler("CoverageSampler").withBlend(BlendFunction.ADDITIVE).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE = new Matrix4f();
    private GpuBuffer uniform;
    private GpuBuffer vertex;
    private ByteBuffer data;
    private final MaskCoveragePipeline coverage = new MaskCoveragePipeline();

    public void draw(GpuTextureView target, GpuTextureView mask, int width, int height, float radius, float intensity) {
        if (target == null || mask == null || width <= 0 || height <= 0) {
            return;
        }
        this.ensure();
        GpuTextureView coverageView = this.coverage.draw(mask, false);
        this.data.clear();
        this.data.putFloat(width).putFloat(height).putFloat(radius).putFloat(intensity).flip();
        if (this.uniform == null || this.uniform.size() < (long)this.data.remaining()) {
            if (this.uniform != null) {
                this.uniform.close();
            }
            this.uniform = RenderSystem.getDevice().createBuffer(() -> "recoin:shader_esp_glow_uniform", 136, (long)this.data.remaining());
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.uniform.slice(), this.data);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().method_71106((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)MODULATOR, (Vector3fc)OFFSET, (Matrix4fc)TEXTURE);
        class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
        class_12137 nearest = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
        try (RenderPass pass = encoder.createRenderPass(() -> "recoin:shader_esp_glow_pass", target, OptionalInt.empty());){
            pass.setPipeline(PIPELINE);
            pass.setVertexBuffer(0, this.vertex);
            pass.bindTexture("MaskSampler", mask, sampler);
            pass.bindTexture("CoverageSampler", coverageView, nearest);
            RenderSystem.bindDefaultUniforms((RenderPass)pass);
            pass.setUniform("DynamicTransforms", transforms);
            pass.setUniform("GlowData", this.uniform);
            pass.draw(0, 6);
        }
    }

    public void close() {
        this.coverage.close();
        if (this.uniform != null) {
            this.uniform.close();
        }
        if (this.vertex != null) {
            this.vertex.close();
        }
        if (this.data != null) {
            MemoryUtil.memFree((Buffer)this.data);
        }
        this.uniform = null;
        this.vertex = null;
        this.data = null;
    }

    private void ensure() {
        if (this.data != null) {
            return;
        }
        this.data = MemoryUtil.memAlloc((int)32);
        ByteBuffer dummy = MemoryUtil.memAlloc((int)4).putInt(0).flip();
        this.vertex = RenderSystem.getDevice().createBuffer(() -> "recoin:shader_esp_glow_vertex", 32, dummy);
        MemoryUtil.memFree((Buffer)dummy);
    }
}

